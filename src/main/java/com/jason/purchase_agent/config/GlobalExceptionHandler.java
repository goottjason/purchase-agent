package com.jason.purchase_agent.config;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * IllegalArgumentException (비즈니스 로직에서 orElseThrow 등) 대응
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleJsonProcessing(JsonProcessingException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "요청 데이터 직렬화 오류: " + ex.getOriginalMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * EntityNotFoundException 등, not found 예외 대응
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * 모든 예외(최상위) 잡아서 서버 에러(500)로 반환(예: 예상치 못한 버그)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // 최상위: 모든 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: {}", ex.getMessage(), ex);
        Map<String, Object> result = new HashMap<>();
        result.put("code", "INTERNAL_ERROR");
        result.put("msg", "내부 서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }



}
