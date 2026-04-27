package edu.bu.cs411.group10.curre.run;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/runs")
@CrossOrigin
public class RunController {
    private final RunRepository runRepository;

    public RunController(RunRepository runRepository){
        this.runRepository = runRepository;
    }

    @PostMapping
    public ResponseEntity<Run> createRun(@RequestHeader("X-User-Id") Long userId,
                                         @RequestBody Run run) {
        run.setUserId(userId);
        Run savedRun = runRepository.save(run);
        return new ResponseEntity<>(savedRun, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Run> getAllRuns(@RequestHeader("X-User-Id") Long userId) {
        return runRepository.findByUserId(userId);
    }
}
