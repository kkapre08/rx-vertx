package com.example.vertx.rxvertx_college.datasource;


import java.util.UUID;



public enum Departments {

  COMPUTER_SCIENCE(UUID.randomUUID()),
  ELECTRIC_ENGINEERING(UUID.randomUUID()),
  MECHANICAL_ENGINEERING(UUID.randomUUID()),
  CIVIL_ENGINEERING(UUID.randomUUID()),
  MATHES(UUID.randomUUID());


  public String department_id;

  Departments(UUID department_id) {
    this.department_id = department_id.toString();
  }

  boolean isValidDepartment(String departmentName) {

    try {

      Departments department = Departments.valueOf(departmentName);

      return Boolean.TRUE;

    } catch (Exception e) {

//      Log.debug("Invalid department name: {} "+departmentName);

//      Log.debug(e);

      return Boolean.FALSE;
    }
  }

}
