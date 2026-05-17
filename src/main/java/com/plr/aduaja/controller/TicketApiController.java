package com.plr.aduaja.controller;

import com.plr.aduaja.model.FieldTask;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import com.plr.aduaja.service.FieldTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketApiController {

    @Autowired
    private FieldTaskService fieldTaskService;

    @GetMapping
    public ResponseEntity<List<FieldTask>> getAllTasks() {
        return ResponseEntity.ok(fieldTaskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldTask> getTaskById(@PathVariable String id) {
        Optional<FieldTask> task = fieldTaskService.getTaskById(id);
        return task.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FieldTask>> getTasksByStatus(@PathVariable TaskStatus status) {
        return ResponseEntity.ok(fieldTaskService.getTasksByStatus(status));
    }

    @GetMapping("/petugas/{petugasId}")
    public ResponseEntity<List<FieldTask>> getTasksByPetugas(@PathVariable String petugasId) {
        return ResponseEntity.ok(fieldTaskService.getTasksByOfficer(petugasId));
    }

    @PostMapping
    public ResponseEntity<FieldTask> createTask(@RequestParam String reportId,
                                                 @RequestParam String officerId,
                                                 @RequestParam String assignedById) {
        try {
            FieldTask task = fieldTaskService.createTask(reportId, officerId, assignedById);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<FieldTask> startTask(@PathVariable String id,
                                                @RequestParam(required = false) BigDecimal latitude,
                                                @RequestParam(required = false) BigDecimal longitude) {
        try {
            return ResponseEntity.ok(fieldTaskService.startTask(id, latitude, longitude));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<FieldTask> completeTask(@PathVariable String id) {
        try {
            return ResponseEntity.ok(fieldTaskService.completeTask(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTaskCounts() {
        Map<String, Long> counts = Map.of(
                "baru", fieldTaskService.countByStatus(TaskStatus.BARU),
                "sedang_dikerjakan", fieldTaskService.countByStatus(TaskStatus.SEDANG_DIKERJAKAN),
                "tertunda", fieldTaskService.countByStatus(TaskStatus.TERTUNDA),
                "selesai", fieldTaskService.countByStatus(TaskStatus.SELESAI)
        );
        return ResponseEntity.ok(counts);
    }
}
