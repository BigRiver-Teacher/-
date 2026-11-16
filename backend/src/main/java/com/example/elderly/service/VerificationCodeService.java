package com.example.elderly.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
@Service
public class VerificationCodeService {

    private final Map<String, String> codeStore=new HashMap<>();

    public String generateAndSendCode(String phone){
        String code=String.format("%06d",new Random().nextInt(999999));
       codeStore.put(phone,code);
        System.out.println("发送验证码："+code);
        return code;
    }
    public String getCodeByPhone(String phone){
        return codeStore.get(phone);
    }
    public void removeCode(String phone){
        codeStore.remove(phone);
    }

    public boolean validateCode(String phone,String code){
        String storedCode = codeStore.get(phone);
        if(storedCode!=null&&storedCode.equals(code)){
            codeStore.remove(phone);
            return true;
        }
        return false;
    }
}
