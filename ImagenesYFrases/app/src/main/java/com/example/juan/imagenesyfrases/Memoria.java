package com.example.juan.imagenesyfrases;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by usuario on 20/10/15.
 */
public class Memoria {
    private Context contexto;

    public Memoria(Context contexto) {
        this.contexto = contexto;
    }

    public boolean escribirInterna(String fichero, String cadena, Boolean anadir, String codigo) {
        File miFichero;
        miFichero = new File(contexto.getFilesDir(), fichero);
        return escribir(miFichero, cadena, anadir, codigo);
    }

    private boolean escribir(File fichero, String cadena, Boolean anadir, String codigo) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter out = null;
        boolean correcto = false;
        try {
            fos = new FileOutputStream(fichero, anadir);
            osw = new OutputStreamWriter(fos, codigo);
            out = new BufferedWriter(osw);
            out.write(cadena);
        } catch (IOException e) {
            Log.e("Error de E/S", e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                    correcto = true;
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
            }
        }
        return correcto;
    }

    public String mostrarPropiedadesInterna(String fichero) {
        File miFichero;
        miFichero = new File(contexto.getFilesDir(), fichero);
        return mostrarPropiedades(miFichero);
    }

    public String mostrarPropiedades(File fichero) {
        SimpleDateFormat formato = null;
        StringBuffer txt = new StringBuffer();
        try {
            if (fichero.exists()) {
                txt.append("Nombre: " + fichero.getName() + '\n');
                txt.append("Ruta: " + fichero.getAbsolutePath() + '\n');
                txt.append("Tamaño (bytes): " + Long.toString(fichero.length()) + '\n');
                formato = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
                txt.append("Fecha: " + formato.format(new Date(fichero.lastModified())) + '\n');
            } else
                txt.append("No existe el fichero " + fichero.getName() + '\n');
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            txt.append(e.getMessage());
        }
        return txt.toString();
    }

    public boolean disponibleEscritura() {
        boolean escritura = false;
        //Comprobamos el estado de la memoria externa (tarjeta SD)
        String estado = Environment.getExternalStorageState();
        if (estado.equals(Environment.MEDIA_MOUNTED))
            escritura = true;
        return escritura;
    }

    public boolean disponibleLectura() {
        boolean lectura = false;
        //Comprobamos el estado de la memoria externa (tarjeta SD)
        String estado = Environment.getExternalStorageState();
        if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                || estado.equals(Environment.MEDIA_MOUNTED))
            lectura = true;
        return lectura;
    }

    public String mostrarPropiedadesExterna(String fichero) {
        File miFichero, tarjeta;
        tarjeta = Environment.getExternalStorageDirectory();
        miFichero = new File(tarjeta.getAbsolutePath(), fichero);
        return mostrarPropiedades(miFichero);
    }

    public boolean escribirExterna(String fichero, String cadena, Boolean anadir, String codigo) {
        File miFichero, tarjeta;
        tarjeta = Environment.getExternalStorageDirectory();
        //tarjeta = Environment.getExternalStoragePublicDirectory("datos/programas/");
        //tarjeta.mkdirs();
        miFichero = new File(tarjeta.getAbsolutePath(), fichero);
        return escribir(miFichero, cadena, anadir, codigo);
    }

    public Resultado leerInterna(String fichero, String codigo) {
        File miFichero;
        //mifichero = new File(getApplicationContext().getFilesDir(), nombreFichero);
        miFichero = new File(contexto.getFilesDir(), fichero);
        return leer(miFichero, codigo);
    }

    private Resultado leer(File fichero, String codigo) {
        FileInputStream fis = null;
        InputStreamReader isw = null;
        BufferedReader in = null;
        //String linea;
        StringBuilder miCadena = new StringBuilder();
        //String codigo = "UTF-8";
        Resultado resultado = new Resultado();
        int n;
        resultado.setCodigo(true);
        try {
            fis = new FileInputStream(fichero);
            isw = new InputStreamReader(fis, codigo);
            in = new BufferedReader(isw);
            while ((n = in.read()) != -1)
                miCadena.append((char) n);
            //while ((linea = in.readLine()) != null)
            //miCadena.append(linea).append('\n');Leer de un fichero en memoria interna (III)
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                    resultado.setContenido(miCadena.toString());
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
                resultado.setCodigo(false);
                resultado.setMensaje(e.getMessage());
            }
        }
        return resultado;
    }

    public Resultado leerExterna(String fichero, String codigo) {
        File miFichero, tarjeta;
        //tarjeta = Environment.getExternalStoragePublicDirectory("datos/programas/");
        tarjeta = Environment.getExternalStorageDirectory();
        miFichero = new File(tarjeta.getAbsolutePath(), fichero);
        return leer(miFichero, codigo);
    }

    public Resultado leerRaw(String fichero) {
        //fichero tendrá el nombre del fichero raw sin la extensión
        InputStream is = null;
        StringBuilder miCadena = new StringBuilder();
        int n;
        Resultado resultado = new Resultado();
        resultado.setCodigo(true);
        try {
//is = contexto.getResources().openRawResource(R.raw.numero);
            is = contexto.getResources().openRawResource(contexto.getResources().getIdentifier(fichero, "raw", contexto.getPackageName()));
            while ((n = is.read()) != -1) {
                miCadena.append((char) n);
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                    resultado.setContenido(miCadena.toString());
                }
            } catch (Exception e) {
                Log.e("Error al cerrar", e.getMessage());
                resultado.setCodigo(false);
                resultado.setMensaje(e.getMessage());
            }
        }
        return resultado;
    }

    public Resultado leerAsset(String fichero) {
        AssetManager am = contexto.getAssets();
        InputStream is = null;
        StringBuilder miCadena = new StringBuilder();
        int n;
        Resultado resultado = new Resultado();
        resultado.setCodigo(true);
        try {
            is = am.open(fichero);
            while ((n = is.read()) != -1) {
                miCadena.append((char) n);
            }
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                    resultado.setContenido(miCadena.toString());
                }
            } catch (Exception e) {
                Log.e("Error al cerrar", e.getMessage());
                resultado.setCodigo(false);
                resultado.setMensaje(e.getMessage());
            }
        }
        return resultado;
    }
}
