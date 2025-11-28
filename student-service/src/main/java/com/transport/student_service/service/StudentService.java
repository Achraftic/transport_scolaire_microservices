package com.transport.student_service.service;

import com.transport.student_service.dto.StudentRequest;
import com.transport.student_service.dto.StudentResponse;
import com.transport.student_service.entity.Student;
import com.transport.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentResponse createStudent(StudentRequest studentRequest) {
        Student student = Student.builder()
                .firstName(studentRequest.getFirstName())
                .lastName(studentRequest.getLastName())
                .latitude(studentRequest.getLatitude())
                .longitude(studentRequest.getLongitude())
                .build();

        student = studentRepository.save(student);
        log.info("Student {} created", student.getId());
        return mapToStudentResponse(student);
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    public StudentResponse getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(this::mapToStudentResponse)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public StudentResponse updateStudent(Long id, StudentRequest studentRequest) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        student.setFirstName(studentRequest.getFirstName());
        student.setLastName(studentRequest.getLastName());
        student.setLatitude(studentRequest.getLatitude());
        student.setLongitude(studentRequest.getLongitude());

        student = studentRepository.save(student);
        log.info("Student {} updated", student.getId());
        return mapToStudentResponse(student);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
        log.info("Student {} deleted", id);
    }

    private StudentResponse mapToStudentResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .latitude(student.getLatitude())
                .longitude(student.getLongitude())
                .build();
    }
}
