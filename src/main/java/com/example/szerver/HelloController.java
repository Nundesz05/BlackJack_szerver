package com.example.szerver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class HelloController {
    @FXML private ListView<String> lvList;
    @FXML private TextField BJText ;

    DatagramSocket socket = null;

    private String uzenet = "500 token";

    private String IP = "25.30.182.43";
    private int port=678;
    private int tet=0;

    public void initialize(){
        try {
            socket = new DatagramSocket(678);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                fogad();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void OnSendPressed() {
            kuld(String.valueOf(uzenet), IP, port);
            lvList.getItems().add(uzenet);
    }

    ArrayList<String> Szerverlapok = new ArrayList<>();
    ArrayList<String> Klienslapok = new ArrayList<>();

    private String lapbetu[]={"C","D","H","S"};
    private String lapszam[]={"2","3","4","5","6","7","8","9","A","K","J","Q"};

    private String lap="";

    private void RandomLap(ArrayList<String> lapok) {
        String randomszam = lapszam[(int)(Math.random()*lapszam.length)];
        String randombetu = lapbetu[(int)(Math.random()*lapbetu.length)];
        lap=randomszam+randombetu;
        lapok.add(randomszam+randombetu);


    }
    private void kuld(String uzenet, String ip, int port) {
        try {
            byte[] adat = uzenet.getBytes("utf-8");
            InetAddress ipv4 = Inet4Address.getByName(ip);
            DatagramPacket packet = new DatagramPacket(adat, adat.length, ipv4, port);
            socket.send(packet);
            System.out.printf("%s:%d -> %s\n", ip, port, uzenet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fogad() {
        byte[] data = new byte[256];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        while (true){
            try {
                socket.receive(packet);
                String uzenet = new String(packet.getData(), 0, packet.getLength(), "utf-8");
                Platform.runLater(() -> onFogad(uzenet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onFogad(String uzenet) {
        String[] u = uzenet.split(":");
        if(u.length>1) {
            if(u[0].equals("join")) {
                lvList.getItems().add(uzenet);
                kuld("joined:"+u[1], IP, port);
            }
            if(u[0].equals("bet")) {

                RandomLap(Szerverlapok);
                kuld("s:"+lap, IP, port);
                RandomLap(Klienslapok);
                kuld("k:"+lap, IP, port);
                RandomLap(Klienslapok);
                kuld("k:"+lap, IP, port);

            }

        } else {
            if(uzenet.equals("hit")) {
                RandomLap(Klienslapok);
                kuld("k:"+lap, IP, port);

            }
            if(uzenet.equals("stand")) {
                RandomLap(Szerverlapok);
                kuld("s:"+lap, IP, port);
            }
        }

    }

}