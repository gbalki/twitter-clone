package com.balki.twitter_clone.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpUtil {

    public String generateOtp() {
        Random random = new Random();
        int randomNumber = random.nextInt(9999999);
        String output = Integer.toString(randomNumber);

        while (output.length() < 7) {
            output = "0" + output;
        }
        return output;
    }
}
