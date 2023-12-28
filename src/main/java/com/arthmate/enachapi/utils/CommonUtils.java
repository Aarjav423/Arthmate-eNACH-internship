package com.arthmate.enachapi.utils;

import com.arthmate.enachapi.exception.EnachRunTimeException;
import com.arthmate.enachapi.exception.XmlSignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class CommonUtils {
    private static PublicKey npciPublicKey;
    private static PublicKey arthmatePublicKey;
    private static PrivateKey arthmatePrivateKey;
    private static String keyStorePath;
    private static String keyStorePassword;

    @Value("${public.key.path}")
    private String npciPublicKeyPath;
    @Value("${arthmate.privatekey.path}")
    private String pfxKeystorePath;
    @Value("${arthmate.privatekey.password}")
    private String pfxKeystorePassword;

    /*
    method to encrypt a String input using RSA algorithm and public key
     */
    public static String encryptString(String valueToEncrypt) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "SunJCE");
        rsaCipher.init(Cipher.ENCRYPT_MODE, npciPublicKey);
        byte[] encryptedKeyBytes = rsaCipher.doFinal(valueToEncrypt.getBytes());
        String encryptedKey = Base64.getEncoder().encodeToString(encryptedKeyBytes);
        log.debug("Encrypted key {}", encryptedKey);
        return encryptedKey;
    }

    public static String decryptString(String encryptedString) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "SunJCE");
        cipher.init(Cipher.DECRYPT_MODE, arthmatePrivateKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedString);
        // Decrypt the encrypted bytes
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        // Convert the decrypted bytes to a string
        String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
        log.debug("Decrypted key {}", decryptedString);
        return decryptedString;
    }

    /*
    method to encode the xml content
     */
    public static String encodeXmlData(String xmlData) {
        return StringEscapeUtils.escapeXml10(xmlData);
    }

    /*
   method to decode the encoded xml content
    */
    public static String decodeXmlData(String encodedXmlData) {
        return StringEscapeUtils.unescapeXml(encodedXmlData);
    }

    /*
   method to get a signed XML string
    */
    public static String generateXMLDigitalSignature(String input) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc;
            try {
                doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(input)));
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                throw new EnachRunTimeException("erorr in reading the xml string");
            }
            //Create XML Signature Factory
            XMLSignatureFactory xmlSigFactory = XMLSignatureFactory.getInstance("DOM");
            PrivateKey privateKey = arthmatePrivateKey;
            DOMSignContext domSignCtx = new DOMSignContext(privateKey, doc.getDocumentElement());
            Reference ref;
            SignedInfo signedInfo;
            try {
                ref = xmlSigFactory.newReference("", xmlSigFactory.newDigestMethod(DigestMethod.SHA256, null),
                        Collections.singletonList(xmlSigFactory.newTransform(Transform.ENVELOPED,
                                (TransformParameterSpec) null)), null, null);
                signedInfo = xmlSigFactory.newSignedInfo(
                        xmlSigFactory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                                (C14NMethodParameterSpec) null),
                        xmlSigFactory.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                        Collections.singletonList(ref));
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
                throw new EnachRunTimeException(ex.getMessage());
            }
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
            KeyStore.PrivateKeyEntry keyEntry =
                    (KeyStore.PrivateKeyEntry) ks.getEntry
                            ("1", new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));
            X509Certificate cert = (X509Certificate) keyEntry.getCertificate();

            // Create the KeyInfo containing the X509Data.
            KeyInfoFactory kif = xmlSigFactory.getKeyInfoFactory();
            var x509Content = new ArrayList<>();
            x509Content.add(cert.getSubjectX500Principal().getName());
            x509Content.add(cert);
            X509Data xd = kif.newX509Data(x509Content);
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

            //Create a new XML Signature
            XMLSignature xmlSignature = xmlSigFactory.newXMLSignature(signedInfo, ki);
            try {
                //Sign the document
                xmlSignature.sign(domSignCtx);
            } catch (MarshalException | XMLSignatureException ex) {
                throw new EnachRunTimeException("Error in signing the document. Error :-" + ex.getMessage());
            }
            //Store the digitally signed document inta a location
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer trans;
            StringWriter writer = new StringWriter();
            try {
                trans = transFactory.newTransformer();
                trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                StreamResult streamRes = new StreamResult(writer);
                trans.transform(new DOMSource(doc), streamRes);
            } catch (TransformerException e) {
                throw new EnachRunTimeException(e.getMessage());
            }
            log.info("XML file with attached digital signature generated successfully ...");
            return writer.getBuffer().toString().replace("&#13;", "").replaceAll("[\\r\\n]+", "");
        } catch (Exception e) {
            throw new XmlSignatureException(e.getMessage());
        }
    }

    public static boolean isXmlDigitalSignatureValid(String input){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = null;
            try {
                doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(input)));
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                throw new EnachRunTimeException("error in reading the signed xml string");
            }
            NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (nl.getLength() == 0) {
                throw new Exception("No XML Digital Signature Found, document is discarded");
            }
            PublicKey publicKey = arthmatePublicKey;
            DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = fac.unmarshalXMLSignature(valContext);
            return signature.validate(valContext);
        } catch (Exception e) {
            throw new XmlSignatureException(e.getMessage());
        }
    }

    public static String generateHashString(String hashSrc) {
        // LOGGER.info("Value Before hashing :" + MMS_Constants.LOG_DELIMETER + hashSrc);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(hashSrc.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new EnachRunTimeException(e.getMessage());
        }

        byte[] byteData = md.digest();

        //convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
        for (byte byteDatum : byteData) {
            sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
        }


        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (byte byteDatum : byteData) {
            String hex = Integer.toHexString(0xff & byteDatum);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return sb.toString();
    }


    @PostConstruct
    private void initialiseNpciPublicKey() throws IOException, CertificateException {
        InputStream fis = new FileInputStream(npciPublicKeyPath);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
        fis.close();
        npciPublicKey = cert.getPublicKey();
    }

    @PostConstruct
    private void initialiseArthmateKeys() throws Exception {
        InputStream fis = new FileInputStream(pfxKeystorePath);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(fis, pfxKeystorePassword.toCharArray());
        fis.close();

        String alias = keyStore.aliases().nextElement();
        Key key = keyStore.getKey(alias, pfxKeystorePassword.toCharArray());
        arthmatePrivateKey = (PrivateKey) key;
        log.info("privateKey {} ", Base64.getEncoder().encodeToString(arthmatePrivateKey.getEncoded()));
        Certificate[] certificateChain = keyStore.getCertificateChain(alias);
        X509Certificate certificate = (X509Certificate) certificateChain[0];
        arthmatePublicKey = certificate.getPublicKey();
        log.info("publicKey {} ", Base64.getEncoder().encodeToString(arthmatePublicKey.getEncoded()));
    }

    @PostConstruct
    private void initialiseKeyStoreDetails() {
        keyStorePassword = pfxKeystorePassword;
        keyStorePath = pfxKeystorePath;
    }

    public static String shuffle(String input){
        List<Character> characters = new ArrayList<>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(!characters.isEmpty()){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }

}
