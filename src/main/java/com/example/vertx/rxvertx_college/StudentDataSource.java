package com.example.vertx.rxvertx_college;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

public class StudentDataSource {



  public static JsonArray getStudents() {

    JsonObject student1 = new JsonObject();
    student1.put("studentId", UUID.randomUUID().toString());
    student1.put("docType", "STUDENT");
    student1.put("name", "Robert Wallas");
    student1.put("phoneNumber",new Random().nextLong());
    student1.put("DepartmentName", Departments.COMPUTER_SCIENCE);
    student1.put("DepartmentID", Departments.COMPUTER_SCIENCE.department_id);

    JsonObject student2 = new JsonObject();
    student2.put("studentId", UUID.randomUUID().toString());
    student2.put("docType", "STUDENT");
    student2.put("name", "Chris Wallas");
    student2.put("phoneNumber",new Random().nextLong());
    student2.put("DepartmentName", Departments.COMPUTER_SCIENCE);
    student2.put("DepartmentID", Departments.COMPUTER_SCIENCE.department_id);


    JsonObject student3 = new JsonObject();
    student3.put("studentId", UUID.randomUUID().toString());
    student3.put("docType", "STUDENT");
    student3.put("name", "Peter Parker");
    student3.put("phoneNumber",new Random().nextLong());
    student3.put("DepartmentName", Departments.MECHANICAL_ENGINEERING);
    student3.put("DepartmentID", Departments.MECHANICAL_ENGINEERING.department_id);


    JsonObject student4 = new JsonObject();
    student4.put("studentId", UUID.randomUUID().toString());
    student4.put("docType", "STUDENT");
    student4.put("name", "Ramesh Choudhary");
    student4.put("phoneNumber",new Random().nextLong());
    student4.put("DepartmentName", Departments.CIVIL_ENGINEERING);
    student4.put("DepartmentID", Departments.CIVIL_ENGINEERING.department_id);


    JsonObject student5 = new JsonObject();
    student5.put("studentId", UUID.randomUUID().toString());
    student5.put("docType", "STUDENT");
    student5.put("name", "Lisa Parker");
    student5.put("phoneNumber",new Random().nextLong());
    student5.put("DepartmentName", Departments.ELECTRIC_ENGINEERING);
    student5.put("DepartmentID", Departments.ELECTRIC_ENGINEERING.department_id);

    JsonArray students = new JsonArray();
    students.add(student1);
    students.add(student2);
    students.add(student3);
    students.add(student4);
    students.add(student5);

    return students;

  }

}
