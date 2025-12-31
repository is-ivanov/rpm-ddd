package by.iivanov.rpm.shared.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
class EmptyStringToNullTestController {

    @GetMapping
    TestRequestPayload get(@RequestParam(required = false) String name) {
        return new TestRequestPayload(name);
    }

    @PostMapping
    TestRequestPayload post(@RequestBody TestRequestPayload payload) {
        return payload;
    }

    record TestRequestPayload(String name) {}
}
