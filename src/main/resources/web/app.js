// Remo dashboard: live samples over server-sent events, an animated loop diagram, a chart and the controls.

const CHART_WINDOW_SECONDS = 60;
const MAX_SAMPLES = 600;
const PROCESS_VALUE_MAX = 150;
const OUTPUT_MAX = 100;
const LINK_HEALTH_WINDOW = 50;
const PACKET_TRAVEL_MS = 700;
const PACKET_EVERY_N_SAMPLES = 2;
const LOST_PACKET_STOP = 0.5;
const INPUT_DEBOUNCE_MS = 180;
// Matches ControllerService: both remote calls must fit in one sample period.
const CALL_TIMEOUT_SHARE = 0.4;

const TANK_TOP = 115;
const TANK_HEIGHT = 150;
const TANK_SETPOINT_BASE_Y = 200;

const State = Object.freeze({
  running: 'RUNNING',
  manual: 'MANUAL',
  holding: 'HOLDING',
  failSafe: 'FAIL_SAFE',
});

const STATE_LABELS = {
  [State.running]: 'RUNNING',
  [State.manual]: 'MANUAL',
  [State.holding]: 'HOLDING',
  [State.failSafe]: 'FAIL-SAFE',
};

const services = [
  { id: 'sensor', title: 'Sensor link · reading', linkId: 'link-reading' },
  { id: 'actuator', title: 'Actuator link · command', linkId: 'link-command' },
];

const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
const darkScheme = window.matchMedia('(prefers-color-scheme: dark)');

const samples = [];
const packets = [];
const faultsByService = new Map();
let settings = null;
let sampleCount = 0;
let chartDirty = true;
let palette = readPalette();

const $ = (id) => document.getElementById(id);

// ---------- API ----------

async function api(path, body) {
  const response = await fetch(path, body === undefined ? {} : {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(payload.error ?? `Request failed with ${response.status}`);
  }

  return payload;
}

async function send(path, body) {
  try {
    return await api(path, body);
  } catch (error) {
    showToast(error.message);
    return null;
  }
}

let toastTimer;
function showToast(message) {
  const toast = $('toast');
  toast.textContent = message;
  toast.classList.add('is-visible');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('is-visible'), 3500);
}

function debounce(callback) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => callback(...args), INPUT_DEBOUNCE_MS);
  };
}

// ---------- Formatting ----------

const formatNumber = (value, digits = 1) => (value === null || value === undefined ? '—' : value.toFixed(digits));
const formatPercent = (value) => `${formatNumber(value, 0)}%`;
const clamp = (value, min, max) => Math.min(max, Math.max(min, value));

// ---------- Samples ----------

function addSample(sample) {
  samples.push(sample);
  if (samples.length > MAX_SAMPLES) {
    samples.shift();
  }
  sampleCount += 1;
  chartDirty = true;

  renderState(sample);
  renderDiagram(sample);
  renderReadouts(sample);
  followServerSetpoint(sample);
  if (sampleCount % PACKET_EVERY_N_SAMPLES === 0) {
    launchPackets(sample);
  }
}

function renderState(sample) {
  document.body.dataset.state = sample.state;
  const badge = $('state-badge');
  badge.dataset.state = sample.state;
  badge.textContent = STATE_LABELS[sample.state] ?? sample.state;

  const callout = $('callout');
  const message = calloutFor(sample);
  callout.classList.toggle('is-visible', message !== null);
  if (message !== null) {
    $('callout-text').textContent = message;
  }
}

function calloutFor(sample) {
  if (sample.state === State.failSafe) {
    return 'Sensor lost too long: fail-safe output';
  }
  if (!sample.sensorOk) {
    return 'Sensor lost: holding the last output';
  }
  if (!sample.actuatorOk) {
    return 'Actuator unreachable: integrator frozen';
  }
  if (sample.state === State.manual) {
    return 'Manual mode: operator sets the output';
  }

  return null;
}

function renderDiagram(sample) {
  const processValue = sample.processValue;
  $('d-sp').textContent = formatNumber(sample.setpoint);
  $('d-pv').textContent = formatNumber(processValue);
  $('d-out').textContent = formatPercent(sample.output);
  // Without an answer the controller cannot know where the valve is; its watchdog may already have closed it.
  $('d-valve').textContent = sample.actuatorOk ? formatPercent(sample.output) : 'no answer';
  $('node-actuator').classList.toggle('is-lost', !sample.actuatorOk);
  $('d-sensor').textContent = sample.sensorOk ? formatNumber(processValue) : 'no answer';
  $('d-tank').textContent = formatNumber(processValue);

  const opening = clamp(sample.output / OUTPUT_MAX, 0, 1);
  $('gauge-fill').style.strokeDasharray = `${opening * 100} 100`;
  $('gauge-needle').style.transform = `rotate(${-90 + 180 * opening}deg)`;

  if (processValue !== null) {
    const level = clamp(processValue / PROCESS_VALUE_MAX, 0, 1);
    $('water').style.transform = `translateY(${TANK_TOP + TANK_HEIGHT * (1 - level)}px)`;
  }
  const setpointLevel = clamp(sample.setpoint / PROCESS_VALUE_MAX, 0, 1);
  const setpointY = TANK_TOP + TANK_HEIGHT * (1 - setpointLevel);
  $('tank-sp').style.transform = `translateY(${setpointY - TANK_SETPOINT_BASE_Y}px)`;

  // The valve and process links flow faster the wider the valve is open.
  for (const flowId of ['flow-valve', 'flow-probe']) {
    const flow = $(flowId);
    flow.style.setProperty('--flow-duration', `${3.2 - 2.6 * opening}s`);
    flow.style.animationPlayState = opening > 0.01 ? 'running' : 'paused';
  }

  $('link-reading').classList.toggle('is-failing', !sample.sensorOk);
  $('link-command').classList.toggle('is-failing', !sample.actuatorOk);
  $('node-sensor').classList.toggle('is-lost', !sample.sensorOk);

  if (sample.sensorOk) {
    const signal = $('sensor-signal');
    signal.classList.remove('is-pinged');
    // Restart the CSS animation on every successful reading.
    void signal.getBoundingClientRect();
    signal.classList.add('is-pinged');
  }
}

function renderReadouts(sample) {
  $('r-error').textContent = sample.processValue === null ? '—' : formatNumber(sample.setpoint - sample.processValue, 2);

  const recent = samples.slice(-LINK_HEALTH_WINDOW);
  renderLinkHealth($('r-sensor'), recent.filter((s) => s.sensorOk).length / recent.length);
  renderLinkHealth($('r-actuator'), recent.filter((s) => s.actuatorOk).length / recent.length);

  $('r-sample').textContent = settings ? `${settings.sampleMillis} ms` : '—';
}

function renderLinkHealth(element, share) {
  element.textContent = `${Math.round(share * 100)}%`;
  element.classList.toggle('is-ok', share >= 0.95);
  element.classList.toggle('is-bad', share < 0.95);
}

function followServerSetpoint(sample) {
  // The demo mode changes the setpoint on the server; keep the slider in step unless the user is dragging it.
  const slider = $('setpoint');
  if (document.activeElement !== slider && Number(slider.value) !== sample.setpoint) {
    slider.value = sample.setpoint;
    $('sp-out').textContent = formatNumber(sample.setpoint, 0);
  }
}

// ---------- Packets ----------

function launchPackets(sample) {
  if (reducedMotion.matches || document.hidden) {
    return;
  }
  spawnPacket('path-reading', 'packet--reading', !sample.sensorOk, 0);
  spawnPacket('path-command', 'packet--command', !sample.actuatorOk, PACKET_TRAVEL_MS * 0.55);
}

function spawnPacket(pathId, className, lost, delayMs) {
  const path = $(pathId);
  const element = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
  element.setAttribute('r', '5');
  element.setAttribute('class', `packet ${className}`);
  element.style.opacity = '0';
  $('packets').append(element);

  packets.push({
    element,
    path,
    length: path.getTotalLength(),
    lost,
    start: performance.now() + delayMs,
  });
}

function animatePackets(now) {
  for (let index = packets.length - 1; index >= 0; index -= 1) {
    const packet = packets[index];
    const progress = (now - packet.start) / PACKET_TRAVEL_MS;
    if (progress < 0) {
      continue;
    }

    const travelled = packet.lost ? Math.min(progress, LOST_PACKET_STOP) : progress;
    const point = packet.path.getPointAtLength(packet.length * clamp(travelled, 0, 1));
    packet.element.setAttribute('cx', point.x);
    packet.element.setAttribute('cy', point.y);

    if (packet.lost && progress >= LOST_PACKET_STOP) {
      // A lost request bursts in the middle of the network.
      const burst = (progress - LOST_PACKET_STOP) / (1 - LOST_PACKET_STOP);
      packet.element.setAttribute('class', 'packet packet--lost');
      packet.element.setAttribute('r', String(5 + burst * 14));
      packet.element.style.opacity = String(Math.max(0, 1 - burst));
    } else {
      packet.element.style.opacity = String(Math.min(1, progress * 6, (1 - progress) * 6));
    }

    if (progress >= 1) {
      packet.element.remove();
      packets.splice(index, 1);
    }
  }
}

// ---------- Chart ----------

function readPalette() {
  const styles = getComputedStyle(document.documentElement);
  const read = (name) => styles.getPropertyValue(name).trim();

  return {
    sp: read('--sp'),
    pv: read('--pv'),
    out: read('--out'),
    hold: read('--hold'),
    safe: read('--safe'),
    grid: read('--grid'),
    text: read('--text-faint'),
    font: read('--mono'),
  };
}

function drawChart() {
  const canvas = $('chart');
  const ratio = window.devicePixelRatio || 1;
  const width = canvas.clientWidth;
  const height = canvas.clientHeight;
  if (canvas.width !== Math.round(width * ratio) || canvas.height !== Math.round(height * ratio)) {
    canvas.width = Math.round(width * ratio);
    canvas.height = Math.round(height * ratio);
  }

  const context = canvas.getContext('2d');
  context.setTransform(ratio, 0, 0, ratio, 0, 0);
  context.clearRect(0, 0, width, height);

  const plot = { left: 34, right: width - 34, top: 10, bottom: height - 22 };
  const lastTime = samples.length > 0 ? samples[samples.length - 1].elapsedSeconds : CHART_WINDOW_SECONDS;
  const startTime = Math.max(0, lastTime - CHART_WINDOW_SECONDS);
  const endTime = startTime + CHART_WINDOW_SECONDS;
  const x = (time) => plot.left + ((time - startTime) / (endTime - startTime)) * (plot.right - plot.left);
  const yValue = (value) => plot.bottom - (value / PROCESS_VALUE_MAX) * (plot.bottom - plot.top);
  const yOutput = (value) => plot.bottom - (value / OUTPUT_MAX) * (plot.bottom - plot.top);

  drawStateBands(context, plot, x, startTime);
  drawGrid(context, plot, x, startTime, endTime);

  const visible = samples.filter((sample) => sample.elapsedSeconds >= startTime);
  drawSeries(context, visible, (sample) => sample.output, x, yOutput, palette.out, 1.5, []);
  drawSeries(context, visible, (sample) => sample.setpoint, x, yValue, palette.sp, 2, [6, 5]);
  drawSeries(context, visible, (sample) => sample.processValue, x, yValue, palette.pv, 2.5, []);
}

function drawStateBands(context, plot, x, startTime) {
  const bandColors = { [State.holding]: palette.hold, [State.failSafe]: palette.safe };
  context.save();
  context.globalAlpha = 0.16;
  for (let index = 0; index < samples.length; index += 1) {
    const sample = samples[index];
    const color = bandColors[sample.state];
    if (!color || sample.elapsedSeconds < startTime) {
      continue;
    }
    const next = samples[index + 1];
    const sampleSeconds = settings ? settings.sampleMillis / 1000 : 0.1;
    const endX = x(next ? next.elapsedSeconds : sample.elapsedSeconds + sampleSeconds);
    context.fillStyle = color;
    context.fillRect(x(sample.elapsedSeconds), plot.top, Math.max(1, endX - x(sample.elapsedSeconds)), plot.bottom - plot.top);
  }
  context.restore();
}

function drawGrid(context, plot, x, startTime, endTime) {
  context.save();
  context.strokeStyle = palette.grid;
  context.fillStyle = palette.text;
  context.lineWidth = 1;
  context.font = `11px ${palette.font}`;

  for (let step = 0; step <= 5; step += 1) {
    const y = plot.top + (step / 5) * (plot.bottom - plot.top);
    context.beginPath();
    context.moveTo(plot.left, y);
    context.lineTo(plot.right, y);
    context.stroke();
    context.textAlign = 'right';
    context.fillText(String(Math.round(PROCESS_VALUE_MAX * (1 - step / 5))), plot.left - 6, y + 4);
    context.textAlign = 'left';
    context.fillText(String(Math.round(OUTPUT_MAX * (1 - step / 5))), plot.right + 6, y + 4);
  }

  context.textAlign = 'center';
  for (let time = Math.ceil(startTime / 10) * 10; time <= endTime; time += 10) {
    context.fillText(`${Math.round(time)}s`, x(time), plot.bottom + 16);
  }
  context.restore();
}

function drawSeries(context, series, valueOf, x, y, color, lineWidth, dash) {
  context.save();
  context.strokeStyle = color;
  context.lineWidth = lineWidth;
  context.lineJoin = 'round';
  context.setLineDash(dash);
  context.beginPath();
  let penDown = false;
  for (const sample of series) {
    const value = valueOf(sample);
    if (value === null) {
      // A lost reading leaves a gap: the chart never pretends there was a value.
      penDown = false;
      continue;
    }
    const pointY = clamp(y(value), 0, context.canvas.clientHeight);
    if (penDown) {
      context.lineTo(x(sample.elapsedSeconds), pointY);
    } else {
      context.moveTo(x(sample.elapsedSeconds), pointY);
      penDown = true;
    }
  }
  context.stroke();
  context.restore();
}

// ---------- Frame loop ----------

function frame(now) {
  animatePackets(now);
  if (chartDirty) {
    drawChart();
    chartDirty = false;
  }
  requestAnimationFrame(frame);
}

// ---------- Controls ----------

function applySettings(next) {
  settings = next;
  const setIfIdle = (id, value) => {
    const input = $(id);
    if (document.activeElement !== input) {
      input.value = value;
    }
  };

  setIfIdle('setpoint', next.setpoint);
  $('sp-out').textContent = formatNumber(next.setpoint, 0);
  setIfIdle('kp', next.kp);
  setIfIdle('ki', next.ki);
  setIfIdle('kd', next.kd);
  setIfIdle('direction', next.direction);
  setIfIdle('manual-output', next.manualOutput);
  $('manual-out').textContent = formatPercent(next.manualOutput);
  $('demo').checked = next.demo;

  for (const button of document.querySelectorAll('[data-mode]')) {
    const isManual = button.dataset.mode === 'manual';
    button.setAttribute('aria-checked', String(isManual === next.manual));
  }
  $('manual-field').classList.toggle('is-disabled', !next.manual);
  $('manual-output').disabled = !next.manual;
  $('timeout-hint').textContent = String(Math.round(next.sampleMillis * CALL_TIMEOUT_SHARE));
}

async function updateSettings(change) {
  const next = await send('/api/settings', change);
  if (next) {
    applySettings(next);
  } else if (settings) {
    applySettings(settings);
  }
}

function bindControls() {
  const postSetpoint = debounce((value) => updateSettings({ setpoint: value }));
  $('setpoint').addEventListener('input', (event) => {
    const value = Number(event.target.value);
    $('sp-out').textContent = formatNumber(value, 0);
    postSetpoint(value);
  });

  const postManualOutput = debounce((value) => updateSettings({ manualOutput: value }));
  $('manual-output').addEventListener('input', (event) => {
    const value = Number(event.target.value);
    $('manual-out').textContent = formatPercent(value);
    postManualOutput(value);
  });

  for (const button of document.querySelectorAll('[data-mode]')) {
    button.addEventListener('click', () => updateSettings({ manual: button.dataset.mode === 'manual' }));
  }

  for (const gain of ['kp', 'ki', 'kd']) {
    $(gain).addEventListener('change', (event) => updateSettings({ [gain]: Number(event.target.value) }));
  }
  $('direction').addEventListener('change', (event) => updateSettings({ direction: event.target.value }));
  $('demo').addEventListener('change', (event) => updateSettings({ demo: event.target.checked }));
}

// ---------- Faults ----------

function buildFaultPanels() {
  const template = $('fault-template');
  for (const service of services) {
    const panel = template.content.firstElementChild.cloneNode(true);
    panel.dataset.service = service.id;
    panel.querySelector('.fault__title').textContent = service.title;
    $('fault-grid').append(panel);

    const postFaults = debounce(() => updateFaults(service.id, readFaultPanel(panel)));
    for (const input of panel.querySelectorAll('[data-fault]')) {
      input.addEventListener('input', () => {
        renderFaultOutputs(panel);
        postFaults();
      });
    }

    const link = $(service.linkId);
    const toggleLink = () => {
      const faults = faultsByService.get(service.id) ?? { down: false, latencyMillis: 0, dropRate: 0 };
      updateFaults(service.id, { ...faults, down: !faults.down });
    };
    link.addEventListener('click', toggleLink);
    link.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        toggleLink();
      }
    });
  }
}

function readFaultPanel(panel) {
  const input = (name) => panel.querySelector(`[data-fault="${name}"]`);

  return {
    down: input('down').checked,
    latencyMillis: Number(input('latencyMillis').value),
    dropRate: Number(input('dropRate').value) / 100,
  };
}

function renderFaultOutputs(panel) {
  const faults = readFaultPanel(panel);
  panel.querySelector('[data-out="latencyMillis"]').textContent = `${faults.latencyMillis} ms`;
  panel.querySelector('[data-out="dropRate"]').textContent = `${Math.round(faults.dropRate * 100)}%`;
}

function applyFaults(serviceId, faults) {
  faultsByService.set(serviceId, faults);
  const service = services.find((candidate) => candidate.id === serviceId);
  $(service.linkId).classList.toggle('is-down', faults.down);

  const panel = document.querySelector(`.fault[data-service="${serviceId}"]`);
  panel.querySelector('[data-fault="down"]').checked = faults.down;
  panel.querySelector('[data-fault="latencyMillis"]').value = faults.latencyMillis;
  panel.querySelector('[data-fault="dropRate"]').value = Math.round(faults.dropRate * 100);
  renderFaultOutputs(panel);
}

async function updateFaults(serviceId, faults) {
  const next = await send(`/api/${serviceId}/faults`, faults);
  const current = next ?? faultsByService.get(serviceId);
  if (current) {
    applyFaults(serviceId, current);
  }
}

// ---------- Process ----------

const plantInputs = {
  gain: 'gain',
  timeConstantSeconds: 'time-constant',
  deadTimeSeconds: 'dead-time',
};

function applyPlant(plant) {
  for (const [field, id] of Object.entries(plantInputs)) {
    $(id).value = plant[field];
  }
}

function bindPlant() {
  for (const id of Object.values(plantInputs)) {
    $(id).addEventListener('change', async () => {
      const plant = Object.fromEntries(
        Object.entries(plantInputs).map(([field, inputId]) => [field, Number($(inputId).value)]),
      );
      const next = await send('/api/plant', plant);
      if (next) {
        applyPlant(next);
      } else {
        applyPlant(await api('/api/plant'));
      }
    });
  }
}

// ---------- Stream ----------

function connectStream() {
  const pill = $('stream-pill');
  const label = $('stream-label');
  const stream = new EventSource('/events');

  stream.addEventListener('open', () => {
    pill.dataset.tone = 'ok';
    label.textContent = 'Live';
  });
  stream.addEventListener('error', () => {
    pill.dataset.tone = 'bad';
    label.textContent = 'Reconnecting…';
  });
  stream.addEventListener('message', (event) => addSample(JSON.parse(event.data)));
}

async function loadInitialState() {
  const [initialSettings, history, plant, ...faults] = await Promise.all([
    api('/api/settings'),
    api('/api/history'),
    api('/api/plant').catch(() => null),
    ...services.map((service) => api(`/api/${service.id}/faults`).catch(() => null)),
  ]);

  applySettings(initialSettings);
  if (plant) {
    applyPlant(plant);
  }
  services.forEach((service, index) => {
    if (faults[index]) {
      applyFaults(service.id, faults[index]);
    }
  });
  for (const sample of history.slice(-MAX_SAMPLES)) {
    samples.push(sample);
  }
  if (samples.length > 0) {
    addSample(samples.pop());
  }
}

function watchColorScheme() {
  darkScheme.addEventListener('change', () => {
    palette = readPalette();
    chartDirty = true;
  });
  window.addEventListener('resize', () => {
    chartDirty = true;
  });
}

async function start() {
  bindControls();
  buildFaultPanels();
  bindPlant();
  watchColorScheme();
  requestAnimationFrame(frame);

  try {
    await loadInitialState();
  } catch (error) {
    showToast(`Could not load the controller state: ${error.message}`);
  }
  connectStream();
}

start();
