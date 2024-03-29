package com.example.hackingproject.signup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.hackingproject.dao.SignupDAO;
import com.example.hackingproject.signup.userentity.UserEntity;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignupService {
	private SignupDAO signupDAO;

    @Value("${rsa_instance}")
    private String RSA_INSTANCE;
	
    @Autowired
    public SignupService(SignupDAO signupDAO) {
        this.signupDAO = signupDAO;
    }
    

	private static final Logger logger = LoggerFactory.getLogger(SignupService.class);
    public Map<String,Object> registerUser(UserEntity userEntity,PrivateKey privateKey) {
        // UserEntity 객체의 데이터를 로그로 출력
        logger.info("Registering userId: {}, UserPw: {}, UserTelno: {}, UserName: {}, UserBirth: {}, UserBank: {}, AccountNumber: {}, UserAgency: {}, AccessLevel: {}, AccountBalance: {}",
                userEntity.getUserId(), userEntity.getUserPw(), userEntity.getUserTelno(), userEntity.getUserName(), userEntity.getUserBirth(),
                userEntity.getUserBank(), userEntity.getAccountNumber(), userEntity.getUserAgency(), userEntity.getAccessLevel(), userEntity.getAccountBalance());

        String userId = "";
        String userPw = "";

        try{
            userId = decryptRsa(privateKey,userEntity.getUserId());
            userPw = decryptRsa(privateKey,userEntity.getUserPw());
        }catch(Exception e){
        }
        
        UserEntity decryptionData = new UserEntity();
        decryptionData.setUserId(userId);
        decryptionData.setUserPw(userPw);

        userEntity.setUserId(userId);
        userEntity.setUserPw(SHA256Encrypt(userPw));
        signupDAO.insertUser(userEntity);

        Map<String, Object> result = new HashMap<String,Object>();
        result.put("userEntity",userEntity);
        result.put("decryptionData",decryptionData);
        result.put("sha256pass",userEntity.getUserPw());
        return result;
    }

    /**
     * 복호화
     *
     * @param privateKey
     * @param securedValue
     * @return
     * @throws Exception
     */
    private String decryptRsa(PrivateKey privateKey, String securedValue) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_INSTANCE);
        byte[] encryptedBytes = hexToByteArray(securedValue);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decryptedValue = new String(decryptedBytes, StandardCharsets.UTF_8); // 문자 인코딩 주의.
        return decryptedValue;
    }

    /**
     * 16진 문자열을 byte 배열로 변환한다.
     *
     * @param hex
     * @return
     */
    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) { return new byte[] {}; }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            byte value = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            bytes[(int) Math.floor(i / 2)] = value;
        }
        return bytes;
    }

    /**
     * SHA-256 암호화
     *
     * @param pass
     * @return String
     */
    public static String SHA256Encrypt(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(pass.getBytes());
            byte[] byteData = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

}



