package med.controller;

import med.entity.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class BasicController {

    public Random random;

    public BasicController() {
        random = new Random(1);
    }

    @GetMapping(value = "/")
    public BaseResponse hello() {
        return new BaseResponse(random.nextInt(100), "Hi there");
    }
}

