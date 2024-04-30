package com.example.szerver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class HelloController {
    @FXML private ListView<String> lvList;
    DatagramSocket socket = null;



    private int port=678;


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

    ArrayList<String> Szerverlapok = new ArrayList<>();
    ArrayList<String> Klienslapok = new ArrayList<>();


    private String lapbetu[]={"C","D","H","S"};
    private String lapszam[]={"2","3","4","5","6","7","8","9","A","K","J","Q"};

    private String lap="";

    private void RandomLap(ArrayList<String> lapok) {
        String randomszam = lapszam[(int)(Math.random()*lapszam.length)];
        String randombetu = lapbetu[(int)(Math.random()*lapbetu.length)];
        lap=randomszam+randombetu;
        lapok.add(randomszam);


    }

    private int ertek(ArrayList<String> lapok) {
        int ertek=0;


        for(int i =0;i<lapok.size();i++ ) {
            if(lapok.get(i).equals("A")) {
                if (ertek + 11 > 21) {
                    ertek += 1;
                } else {
                    ertek += 11;
                }
            }
            else if(lapok.get(i).equals("Q") || lapok.get(i).equals("J") || lapok.get(i).equals("K")) {
                ertek += 10;
            }

            else {
                ertek+=Integer.parseInt(lapok.get(i));
            }
        }

        return ertek;
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
                String ip = packet.getAddress().getHostAddress();
                Platform.runLater(() -> onFogad(uzenet,ip));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void osztas(String ip){
        kliensbalance=kliensbalance-klienstet;
        if (ertek(Szerverlapok) >21) {
            kuld("ertekS:"+String.valueOf(ertek(Szerverlapok)),ip,port);
            kuld("balance:"+(kliensbalance+(klienstet*2)),ip,port);
            kliensbalance=kliensbalance+klienstet*2;
            kuld("end",ip,port);
            kuld("end",ip,port);

        } else {
            kuld("ertekS:"+String.valueOf(ertek(Szerverlapok)),ip,port);
            if(ertek(Klienslapok)> ertek(Szerverlapok)) {
                kuld("balance:" + (kliensbalance + (klienstet*2)), ip, port);
                kliensbalance=kliensbalance+(klienstet*2);
                kuld("end", ip, port);
            }
            else if(ertek(Klienslapok)==ertek(Szerverlapok)) {

                kuld("balance:" + (kliensbalance+klienstet), ip, port);
                kuld("end", ip, port);
                kliensbalance=kliensbalance+klienstet;

            } else {
                kuld("balance:"+(kliensbalance),ip,port);
                kuld("end",ip,port);
            }

        }
    }

    private int klienszseton=0;
    private int klienstet=0;
    private int kliensbalance=0;


    private void onFogad(String uzenet,String ip) {
        String[] u = uzenet.split(":");
        if(u.length>1) {
            if(u[0].equals("join")) {
                lvList.getItems().add(uzenet);
                klienszseton=Integer.parseInt(u[1]);
                kuld("joined:"+u[1],ip, port);
                kliensbalance=klienszseton;

            }
            else if(u[0].equals("bet")) {
                Klienslapok.clear();
                Szerverlapok.clear();
                klienstet=Integer.parseInt(u[1]);
                RandomLap(Szerverlapok);
                kuld("s:"+lap, ip, port);
                RandomLap(Klienslapok);
                kuld("k:"+lap, ip, port);
                RandomLap(Klienslapok);
                kuld("k:"+lap, ip, port);
                kuld("ertekK:"+String.valueOf(ertek(Klienslapok)),ip,port);
                kuld("ertekS:"+String.valueOf(ertek(Szerverlapok)),ip,port);
            }

        } else {
            if(uzenet.equals("hit")) {
                RandomLap(Klienslapok);
                kuld("k:"+lap, ip, port);
                kuld("ertekK:"+String.valueOf(ertek(Klienslapok)),ip,port);
                if(ertek(Klienslapok) >21) {
                    kuld("balance:"+(kliensbalance-klienstet),ip,port);
                    kliensbalance=kliensbalance-klienstet;
                    kuld("end",ip,port);
                }
            }
            if(uzenet.equals("stand")) {
                while (ertek(Szerverlapok) < 17) {
                    RandomLap(Szerverlapok);
                    kuld("s:"+lap, ip, port);
                };
                osztas(ip);

            }
            if(uzenet.equals("exit")) {
                kuld("paid:"+(kliensbalance-klienstet),ip,port);
            }
        }

    }

}