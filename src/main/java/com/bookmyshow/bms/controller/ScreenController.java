package com.bookmyshow.bms.controller;

import com.bookmyshow.bms.dto.ScreenDto;
import com.bookmyshow.bms.entity.Screen;
import com.bookmyshow.bms.service.ScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    @PostMapping
    public ResponseEntity<ScreenDto> createScreen(@RequestBody ScreenDto screenDto) {
        ScreenDto createScreen=screenService.createScreen(screenDto);
        return ResponseEntity.ok(createScreen);
    }

    @GetMapping()
    public ResponseEntity<List<ScreenDto>> getAllScreens(){
        return ResponseEntity.ok(screenService.getAllScreen());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreenDto> getScreen(@PathVariable Long id) {
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity <List<ScreenDto>> getByTheaterId(@PathVariable Long theaterId){
        List<ScreenDto> screens=screenService.getScreensByTheaterId(theaterId);
        return ResponseEntity.ok(screens);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScreenDto> updateScreen(
            @PathVariable Long id,
            @RequestBody ScreenDto screenDto
    ) {
        return ResponseEntity.ok(screenService.updateScreen(id, screenDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.ok("Screen deleted successfully with the id:"+id);
    }
}
