package com.transport.student_service.controller;

import com.transport.student_service.dto.StudentRequest;
import com.transport.student_service.dto.StudentResponse;
import com.transport.student_service.service.StudentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@RequestBody StudentRequest studentRequest) {
        StudentResponse createdStudent = studentService.createStudent(studentRequest);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents(HttpServletRequest request) {
        String username = request.getHeader("X-Auth-User");
        String roles = request.getHeader("X-Auth-Roles");
        String userId = request.getHeader("X-Auth-UserId");
        System.out.println("Authenticated user: " + username);
        System.out.println("Roles: " + roles);
        System.out.println("User ID: " + userId);
        List<StudentResponse> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
            @RequestBody StudentRequest studentRequest) {
        StudentResponse updatedStudent = studentService.updateStudent(id, studentRequest);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
