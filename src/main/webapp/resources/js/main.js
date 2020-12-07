function handleDialogSubmit(xhr, status, args) {
  if (args.validationFailed) {
    slideUpTo(".quiz-header");
  } else {
  }
}

function handleBidSubmit(xhr, status, args) {
  if (args.validationFailed) {
    slideUpTo(".validation-input");
  } else {
    slideUpTo(".page-header.header-div");
  }
}

function slideToSuccessfulOrErrorMessage(xhr, status, args, slideToError, slideToSuccessful, panel) {
  if (args.validationFailed) {
    slideUpTo(slideToError);
  } else {
    slideUpTo(slideToSuccessful);
    if (panel) {
      PF(panel).hide();
    }
  }
}

function slideUpTo(element) {
  if ($(element).size() > 0) {

    $('html, body').animate({
      scrollTop: $(element).offset().top
    }, 500);
  }
}

/*

 $(document).ready(function () {
 window.isActive = true;
 window.polling = false;

 $(window).focus(function () {
 this.isActive = true;
 });
 $(window).blur(function () {
 this.isActive = false;
 });
 initialize();
 });

 function initialize() {
 if (!window.isActive && window.polling) {
 if (typeof(stopPollingDueToBrowserBlur) === "function") {
 stopPollingDueToBrowserBlur();
 window.polling = false;
 }
 }
 else if (window.isActive && !window.polling) {
 if (typeof(startPollingDueToTabSwitchFocus) === "function") {
 startPollingDueToTabSwitchFocus();
 window.polling = true;
 }
 }
 window.setTimeout("initialize()", 2000);
 }
 */
