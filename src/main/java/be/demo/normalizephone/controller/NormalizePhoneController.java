package be.demo.normalizephone.controller;

import be.demo.normalizephone.beans.InputPhone;
import be.demo.normalizephone.beans.OutputPhone;
import be.demo.normalizephone.beans.Phone;
import be.demo.normalizephone.business.NormalizePhone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class NormalizePhoneController {

    private final NormalizePhone phoneService;

    @Autowired
    public NormalizePhoneController(NormalizePhone phoneService) {
        this.phoneService = phoneService;
    }

    @PostMapping(value = "/normalize")
    public Phone normalizePhone(@RequestBody InputPhone inputPhone) throws IOException {
        OutputPhone normalizedPhone = phoneService.normalize(inputPhone);

        return new Phone(inputPhone, normalizedPhone);
    }

}