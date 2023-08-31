package com.example.channelnfcandroid;

import io.flutter.embedding.android.FlutterActivity;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
//necesarias para funcionalidades del nfc y vibración

import android.os.Bundle;
import android.content.Context;
import android.os.Vibrator;
import android.os.VibrationEffect;




// librerias importadas de: implementation 'com.github.devnied.emvnfccard:library:3.0.1'
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
//mas importaciones
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import android.nfc.tech.IsoDep;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example/nfc_channel";
    private MethodChannel methodChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        methodChannel = new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL);
    }

    private void sendNFCDataToFlutter(String idContentString, String processState) {
        methodChannel.invokeMethod("onNFCDataChanged", idContentString + "|" + processState);
    }


    @Override
    public void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
                @Override
                public void onTagDiscovered(Tag tag) {
                    vibrate();
                    String processState = "";
                    processState = "Comenzando";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendNFCDataToFlutter("", "Comenzando...");
                        }
                    });
                    IsoDep isoDep = null;
                    try {
                        isoDep = IsoDep.get(tag);
                        if (isoDep != null) {
                            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
                        }
                        //Log.d("NFC DATA", "Iniciando ");
                        processState = "Leyendo";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendNFCDataToFlutter("", "Leyendo...");
                            }
                        });
                        isoDep.connect();

                        byte[] response;
                        String idContentString = "Datos de tarjeta:";

                        PcscProvider provider = new PcscProvider();
                        provider.setmTagCom(isoDep);

                        EmvTemplate.Config config = EmvTemplate.Config()
                                .setContactLess(true)
                                .setReadAllAids(true)
                                .setReadTransactions(true)
                                .setRemoveDefaultParsers(false)
                                .setReadAt(true);

                        EmvTemplate parser = EmvTemplate.Builder()
                                .setProvider(provider)
                                .setConfig(config)
                                .build();

                        EmvCard card = parser.readEmvCard();
                        processState = "Obteniendo datos...";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendNFCDataToFlutter("", "Obteniendo datos...");
                            }
                        });
                        String cardNumber = card.getCardNumber();
                        //idContentString = idContentString + "\n cardnumber " + card.toString();
                        Date expireDate = card.getExpireDate();
                        LocalDate date = LocalDate.of(1999, 12, 31);
                        if (expireDate != null) {
                            date = expireDate.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                        }
                        EmvCardScheme cardGetType = card.getType();
                        if (cardGetType != null) {
                            String typeName = card.getType().getName();
                            String[] typeAids = card.getType().getAid();
                            idContentString = idContentString + "\n" + "typeName: " + typeName;
                            for (int i = 0; i < typeAids.length; i++) {
                                idContentString = idContentString + "\n" + "aid " + i + " : " + typeAids[i];
                            }
                        }

                        List<Application> applications = card.getApplications();
                        idContentString = idContentString + "\n" + "cardNumber: " + prettyPrintCardNumber(cardNumber);
                        idContentString = idContentString + "\n" + "expireDate: " + date;


                        //Log.d("NFC DATA", "Resultado ===" + finalIdContentString);

                        //Log.d("NFC DATA", "Resultado === Succesful");

                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(100, 100));
                        String finalIdContentString = idContentString;
                        String finalProcessState = processState;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendNFCDataToFlutter(finalIdContentString, "Succesful.");
                            }
                        });
                        try {

                            isoDep.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        Log.d("NFC DATA", "Result === Fallo!");
                        processState = "Fail al leer tarjeta";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendNFCDataToFlutter("", "Fail al leer tarjeta");
                            }
                        });
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(100, 100));
                        //Trying to catch any ioexception that may be thrown
                        e.printStackTrace();
                    } catch (Exception e) {
                        Log.d("NFC DATA", "Result === Fallo!");
                        processState = "Fail 2";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendNFCDataToFlutter("", "Fail 2");
                            }
                        });
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(100, 100));
                        //Trying to catch any exception that may be thrown
                        e.printStackTrace();
                    }

                    // Puedes enviar una notificación al código Flutter aquí, si es necesario
                }
            }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B, null);
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(vibrationEffect);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public static String prettyPrintCardNumber(String cardNumber) {
        if (cardNumber == null) return null;
        char delimiter = ' ';
        return cardNumber.replaceAll(".{4}(?!$)", "$0" + delimiter);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

}
