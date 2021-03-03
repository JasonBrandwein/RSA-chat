package com.release.rsa_20;

import android.os.Build;

import androidx.annotation.RequiresApi;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;
//todo
//to encrypt we need the message and then we do a big int fucnction. Then we can send that value into the encrypt function and upload that base64 string to the db.
//When you pull the messages for each one we have to send it into the decoder and it will return a string which is the decrypted message.
//We must pull the public key for encryption of the user
//we must pull your own private key from the device storage for decrypting your message.
//This is extremely insecure because you can do a coppersmith attack.

public class rsa {
    @Override
    public String toString() {
        return "rsa{" +
                "n=" + n +
                ", privatekey=" + privatekey +
                '}';
    }

    BigInteger message;
    BigInteger p;
    BigInteger q;
    //n = p*q
    private BigInteger n =new BigInteger("1");
    //E = "65537" Commonly used for rsa. // Also the public key
    private BigInteger pubkey=new BigInteger("65537");
    //Phi is used to find your privatekey
    private BigInteger phi=new BigInteger("1");
    private BigInteger privatekey=new BigInteger("1");

    public String getN() {
        return n.toString();
    }

    public void setN(BigInteger n) {
        this.n = n;
    }

    public BigInteger getPubkey() {
        return pubkey;
    }

    public void setPubkey(BigInteger pubkey) {
        this.pubkey = pubkey;
    }

    public BigInteger getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(BigInteger privatekey) {
        this.privatekey = privatekey;
    }

    public rsa() {
    }

    public rsa(BigInteger n, BigInteger privatekey) {
        this.n = n;
        this.privatekey = privatekey;
    }

    public void printvals() {
        System.out.println("p = " + p + "\nq = " + q + "\nn = " + n + "\npubkey = " + pubkey + "\nphi = " + phi + "\nPrivatekey = " + privatekey);
    }

    //Gives you all the primes that you'll need within a range that you specify..
    public BigInteger gen_primes() {
        int range = 2048;
        SecureRandom random = new SecureRandom();
        //2048 bits
        BigInteger newprime = BigInteger.probablePrime(range, random);
        return newprime;
    }

    //Not used. BigInt contains all the functions that we need.
    public BigInteger lcm(BigInteger a, BigInteger b) {
        BigInteger mul = a.multiply(b);
        BigInteger gcd = a.gcd(b);
        BigInteger lcm = mul.divide(gcd);
        return lcm;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String base64encoder(BigInteger string) {
        Base64.Encoder encoder = Base64.getUrlEncoder();
        byte[] bigintbyte = string.toByteArray();
        String encoded= encoder.encodeToString(bigintbyte);
        return encoded;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)


    public static BigInteger base64decoder(String string) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        //Something went wrong here.
        //System.out.println("String = " + string);
        byte[] decodebyte = decoder.decode(string.trim());
        //System.out.println("Decodedbyte = " + decodebyte);
        BigInteger decodedbigint = new BigInteger(decodebyte);
        return decodedbigint;
    }

    public BigInteger encrypt(String string) {
        System.out.println("String = " + string);
        BigInteger encrypted = new BigInteger(string.getBytes());
        printvals();
        encrypted = encrypted.modPow(pubkey, n);
        System.out.println("Encrypted string = \n" + encrypted);
        return encrypted;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String send_encrypt(String string, BigInteger pub_key) {
        System.out.println("String = " + string);
        BigInteger encrypted = new BigInteger(string.getBytes());
         encrypted = encrypted.modPow(BigInteger.valueOf(65537), pub_key);
        System.out.println("Encrypted string = \n" + encrypted);
        return base64encoder(encrypted);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String send_decrypt(BigInteger message, String privatekey, String pubkey){
        System.out.println("send decrypt started");
        System.out.println("message = " + message + "\nprivatekey = " + privatekey + "\npubkey =" + pubkey);
        //we can now decryp the fucking message holy shit....
        BigInteger decryptedstring = message.modPow(base64decoder(privatekey), base64decoder(pubkey));
        return new String(decryptedstring.toByteArray());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void set_n(String pubkey){
        this.n = base64decoder(pubkey);
    }







    public String decrypt(BigInteger encrypted_string) {
        BigInteger decryptedstring = encrypted_string.modPow(privatekey, n);
        return new String(decryptedstring.toByteArray());
    }


    public void genkeys() {
        p = gen_primes();
        q = gen_primes();

        phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        n = p.multiply(q);
        privatekey = pubkey.modInverse(phi);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String get_publickey(){
        //n is essentially the pub key, pub key is hardcoded as 65537
        System.out.println(this.n);
        System.out.println(base64encoder(this.n));
        System.out.println(base64decoder(base64encoder(this.n)));
        return (base64encoder(this.n));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String get_privatekey(){
        return (base64encoder(this.privatekey));
    }
}
