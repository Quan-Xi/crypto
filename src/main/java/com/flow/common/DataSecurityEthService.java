package com.flow.common;

import com.flow.exception.ErrorCodeEnum;
import com.flow.exception.ExceptionUtils;
import com.flow.tool.crypto.ECDSA;
import com.flow.tool.crypto.Secp256k1;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Scanner;

@Service
public class DataSecurityEthService {

    public static final Secp256k1 SECP256K1 = Secp256k1.getInstance();

    @Value("${security.privateKey}")
    private String privateKey;
    private ECKeyPair ecKeyPair;

    @PostConstruct
    public void init(){
        if (StringUtils.isEmpty(privateKey)) return;
        try {
            ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        } catch (Exception exception) {
            ExceptionUtils.printStackTrace(exception, "DataSecurityEthService.init error");
        }
    }

    public  BigInteger bigIntegerPrivateKey() {
        if (Objects.isNull(ecKeyPair)) ErrorCodeEnum.NOT_OPEN.throwException();
        return ecKeyPair.getPrivateKey();
    }

    public BigInteger bigIntegerPublicKey() {
        if (Objects.isNull(ecKeyPair)) ErrorCodeEnum.NOT_OPEN.throwException();
        return ecKeyPair.getPublicKey();
    }

    public String decryptWithPrivateKey(String encryptStr){
        try {
            return decryptWithPrivateKey_(encryptStr);
        } catch (Exception ex) {
            ExceptionUtils.printStackTrace(ex);
        }
        return null;
    }

    public String decryptWithPrivateKey_(String encryptStr) throws Exception {
        BCECPrivateKey bcecPrivateKey = SECP256K1.privateKey(bigIntegerPrivateKey());
        ECDSA.Eccrypto eccrypto = new Gson().fromJson(encryptStr, ECDSA.Eccrypto.class);
        return SECP256K1.decryptWithPrivateKey(bcecPrivateKey, eccrypto);
    }

    public String encryptWithPublicKey(String plaintextStr){
        try {
            return encryptWithPublicKey_(plaintextStr);
        } catch (Exception ex) {
            ExceptionUtils.printStackTrace(ex);
        }
        return null;
    }

    public String encryptWithPublicKey_(String plaintextStr) throws Exception {
        BCECPublicKey bcecPublicKey = SECP256K1.publicKey(bigIntegerPublicKey());
        ECDSA.Eccrypto eccrypto = SECP256K1.encryptWithPublicKey(bcecPublicKey, plaintextStr);
        if (Objects.isNull(eccrypto)) return null;
        return new Gson().toJson(eccrypto);
    }

    public static void main(String[] args) throws Exception {
        System.out.print("程序启动\n1: 公钥加密\n2: 私钥解密\n请选择: ");
        Scanner scanner = new Scanner(System.in);
        String key = scanner.nextLine();
        switch (key) {
            case "1":
                System.out.print("输入加密的字符串: ");
                String plaintext = scanner.nextLine();

                System.out.print("输入加密所需公钥: ");
                String pubKey = scanner.nextLine();
                BCECPublicKey bcecPublicKey = SECP256K1.publicKey(new BigInteger(pubKey, 16));
                ECDSA.Eccrypto ecCrypto = SECP256K1.encryptWithPublicKey(bcecPublicKey, plaintext);
                String ecCryptoString = new Gson().toJson(ecCrypto);
                System.out.println("\n加密之后的字符串为: " + ecCryptoString);
                break;
            case "2":
                System.out.print("输入需要解密的字符串: ");
                String encryptStr = scanner.nextLine();

                System.out.print("输入加密所需私钥: ");
                String priKey = scanner.nextLine();
                BCECPrivateKey bcecPrivateKey = SECP256K1.privateKey(new BigInteger(priKey, 16));
                ECDSA.Eccrypto eccrypto = new Gson().fromJson(encryptStr, ECDSA.Eccrypto.class);
                String decrypt = SECP256K1.decryptWithPrivateKey(bcecPrivateKey, eccrypto);
                System.out.println("\n解密之后的字符串为: " + decrypt);
                break;
            default:
                System.out.println("Ha Ha Ha");
        }

    }

}
