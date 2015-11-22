package com.example.juan.imagenesyfrases;


import android.os.SystemClock;

interface OnTicksListener {
    void OnTick(int indice);
}

public class CallBack extends Thread {
    private int _intervalo;
    private int _sFotos;
    private int _sFrases;
    private OnTicksListener _listener;
    private final static int MILLIS = 1000;

    public CallBack(OnTicksListener listener, int intervalo, int tamanioFotos, int tamanioFrases) {
        _intervalo = intervalo;
        _sFotos = tamanioFotos;
        _sFrases = tamanioFrases;
        _listener = listener;
    }

    @Override
    public void run() {
        super.run();
        int max = Math.max(_sFotos, _sFrases);
        for (int i = 0; i < max; i++) {
            _listener.OnTick(i);
            SystemClock.sleep(_intervalo * MILLIS);
        }
    }
}
