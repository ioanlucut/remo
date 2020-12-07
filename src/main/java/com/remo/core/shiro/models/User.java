package com.remo.core.shiro.models;

import com.remo.core.roles.model.Role;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.remo.core.shiro.models.User.*;

@Entity
@SequenceGenerator(name = SEQ_GENERATOR_NAME, sequenceName = SEQ_NAME, initialValue = SEQ_INITIAL_VALUE,
    allocationSize = ALLOCATION_SIZE)
@NamedQueries({@NamedQuery(name = "User.list", query = "SELECT u FROM User u")})
@Table(name = "users")
public class User implements Serializable, Comparable<User> {

  protected static final String SEQ_NAME = "user_id";
  protected static final String SEQ_GENERATOR_NAME = "user_seq_generator";
  protected static final int SEQ_INITIAL_VALUE = 600000;
  protected static final int ALLOCATION_SIZE = 1;
  private static final long serialVersionUID = -1799428438852023627L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_GENERATOR_NAME)
  private Long id;

  @NotNull
  @Column(nullable = false)
  private String firstName;

  @NotNull
  @Column(nullable = false)
  private String lastName;

  /**
   * User is logged based on the email and password (not username and password)
   */
  @NotNull
  @Column(nullable = false, unique = true)
  private String email;

  @NotNull
  @Column(nullable = false)
  private String password;

  /**
   * The user can be enabled or disabled from administration platform.
   */
  private boolean enabled;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date registeredDate;

  @LazyCollection(LazyCollectionOption.FALSE)
  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "shiro_user_roles", joinColumns = {@JoinColumn(name = "user_id")})
  @Column(name = "role")
  private List<Role> roles = new ArrayList<>();

  @PrePersist
  void createdAt() {
    this.registeredDate = new Date();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Date getRegisteredDate() {
    return registeredDate;
  }

  public void setRegisteredDate(Date registeredDate) {
    this.registeredDate = registeredDate;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    User other = (User) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(User o) {
    return getEmail().compareTo(o.getEmail());
  }

}