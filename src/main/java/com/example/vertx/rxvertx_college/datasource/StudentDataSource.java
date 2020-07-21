package com.example.vertx.rxvertx_college.datasource;

import com.example.vertx.rxvertx_college.constants.Constants;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

public class StudentDataSource {


  public static JsonArray getStudents() {

    JsonObject student1 = new JsonObject();
    student1.put(Constants.STUDENT_ID, UUID.randomUUID().toString());
    student1.put(Constants.DOC_TYPE, Constants.STUDENT_DOC_TYPE);
    student1.put(Constants.NAME, "Robert Wallas");
    student1.put(Constants.PHONE_NUMBER, getPhoneNumber());
    student1.put(Constants.DEPARTMENT_NAME, Departments.COMPUTER_SCIENCE);
    student1.put(Constants.DEPARTMENT_ID, Departments.COMPUTER_SCIENCE.department_id);

    JsonObject student2 = new JsonObject();
    student2.put(Constants.STUDENT_ID, UUID.randomUUID().toString());
    student2.put(Constants.DOC_TYPE, Constants.STUDENT_DOC_TYPE);
    student2.put(Constants.NAME, "Chris Wallas");
    student2.put(Constants.PHONE_NUMBER,getPhoneNumber());
    student2.put(Constants.DEPARTMENT_NAME, Departments.COMPUTER_SCIENCE);
    student2.put(Constants.DEPARTMENT_ID, Departments.COMPUTER_SCIENCE.department_id);


    JsonObject student3 = new JsonObject();
    student3.put(Constants.STUDENT_ID, UUID.randomUUID().toString());
    student3.put(Constants.DOC_TYPE, Constants.STUDENT_DOC_TYPE);
    student3.put(Constants.NAME, "Peter Parker");
    student3.put(Constants.PHONE_NUMBER,getPhoneNumber());
    student3.put(Constants.DEPARTMENT_NAME, Departments.MECHANICAL_ENGINEERING);
    student3.put(Constants.DEPARTMENT_ID, Departments.MECHANICAL_ENGINEERING.department_id);


    JsonObject student4 = new JsonObject();
    student4.put(Constants.STUDENT_ID, UUID.randomUUID().toString());
    student4.put(Constants.DOC_TYPE, Constants.STUDENT_DOC_TYPE);
    student4.put(Constants.NAME, "Ramesh Choudhary");
    student4.put(Constants.PHONE_NUMBER,getPhoneNumber());
    student4.put(Constants.DEPARTMENT_NAME, Departments.CIVIL_ENGINEERING);
    student4.put(Constants.DEPARTMENT_ID, Departments.CIVIL_ENGINEERING.department_id);


    JsonObject student5 = new JsonObject();
    student5.put(Constants.STUDENT_ID, UUID.randomUUID().toString());
    student5.put(Constants.DOC_TYPE, Constants.STUDENT_DOC_TYPE);
    student5.put(Constants.NAME, "Lisa Parker");
    student5.put(Constants.PHONE_NUMBER,getPhoneNumber());
    student5.put(Constants.DEPARTMENT_NAME, Departments.ELECTRIC_ENGINEERING);
    student5.put(Constants.DEPARTMENT_ID, Departments.ELECTRIC_ENGINEERING.department_id);

    JsonArray students = new JsonArray();
    students.add(student1);
    students.add(student2);
    students.add(student3);
    students.add(student4);
    students.add(student5);

    return students;

  }

  private static String getPhoneNumber() {
    long number = new Random().nextLong();
    if(number<0) {number *=-1;}
    return String.valueOf(number).substring(0,10);
  }

}
