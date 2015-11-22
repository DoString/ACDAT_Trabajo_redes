package com.example.juan.imagenesyfrases;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button descargar;
    EditText imgUrl;
    EditText fUrl;
    RadioGroup imgRadio;
    RadioGroup fRadio;
    ImageView visor;
    ArrayList<String> errores;
    ArrayList<String> frases;
    ArrayList<String> fotos;
    AsyncHttpClient cliente;
    int _intervalo;
    String USER;
    String PASS;
    Memoria mem;
    final static int PUERTO = 21;
    FTPConnection conexionFtp;
    final static String COD = "UTF-8";
    static String HOST;
    static String FICHERO;
    static String DIRECTORIO;
    final Activity a = this;
    TextView textos;
    CountDownTimer countDownTimer;
    String[] params;
    SyncHttpClient http;
    DateFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        InicializarCampos();
    }

    private void InicializarCampos() {
        descargar = (Button) findViewById(R.id.descargar);
        descargar.setOnClickListener(this);
        imgUrl = (EditText) findViewById(R.id.imgUrl);
        fUrl = (EditText) findViewById(R.id.fUrl);
        imgRadio = (RadioGroup) findViewById(R.id.imgRadio);
        fRadio = (RadioGroup) findViewById(R.id.fRadio);
        errores = new ArrayList<>();
        fotos = new ArrayList<>();
        frases = new ArrayList<>();
        cliente = new AsyncHttpClient();
        http = new SyncHttpClient();
        visor = (ImageView) findViewById(R.id.visor);
        mem = new Memoria(this);
        textos = (TextView) findViewById(R.id.frases);


        Resultado res = mem.leerAsset("config.txt");
        USER = res.getCodigo() ? res.getContenido().split("\r\n")[0] : "";
        PASS = res.getCodigo() ? res.getContenido().split("\r\n")[1] : "";
        res = mem.leerRaw("intervalo");
        _intervalo = res.getCodigo() ? Integer.parseInt(res.getContenido()) : 2000;


        conexionFtp = new FTPConnection();
        params = new String[4];
        int max = Math.max(fotos.size(), frases.size());
        final Activity ac = this;
        df = DateFormat.getDateTimeInstance();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (v == descargar) {
            descargar.setEnabled(false);
            fotos.clear();
            frases.clear();
            errores.clear();
            if (getFURL().isEmpty() || getImgUrl().isEmpty()) {
                Toast.makeText(MainActivity.this, "Debes introducir dos URLs", Toast.LENGTH_SHORT).show();
                descargar.setEnabled(true);
                return;
            }
            if (isIMGHttp()) {
                params[0] = "HTTP";
                params[1] = "http://" + getImgUrl();
            } else {
                params[0] = "FTP";
                params[1] = "ftp://" + getImgUrl();
            }
            if (isFHTTP()) {
                params[2] = "HTTP";
                params[3] = "http://" + getFUrl();
            } else {
                params[2] = "FTP";
                params[3] = "ftp://" + getFURL();
            }

            TareaAsincrona t = new TareaAsincrona();
            t.execute(params);
        }
    }

    private String getFURL() {
        return this.fUrl.getText().toString();
    }


    private String getDirectorio(String url) {
        String dir = ".";
        List<String> temp = Uri.parse(url).getPathSegments();
        for (int i = 1; i < temp.size(); i++) {
            dir += "/" + temp.get(i - 1);
        }
        return dir;
    }

    private Resultado leerFicheroHttp(String... url) {
        HOST = Uri.parse(url[0]).getHost();
        DIRECTORIO = getDirectorio(url[0]);
        FICHERO = Uri.parse(url[0]).getLastPathSegment();

        final Resultado res = new Resultado();

        final String dir = url[0];
        http.get(url[0], new FileAsyncHttpResponseHandler(a) {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                //progreso.dismiss();
                res.setMensaje("ERROR HTTP: " + statusCode + " error al descargar " + dir + " causa : " + throwable.getMessage());
                res.setCodigo(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                // progreso.dismiss();
                //texto.setText("");
                try {
                        /*InputStream file = openFileInput(response.getName());
                        InputStreamReader contenido = new InputStreamReader(file);
                        BufferedReader br = new BufferedReader(contenido);*/

                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String linea;
                    String lineas = "";
                    try {
                        while ((linea = br.readLine()) != null) {
                            lineas += linea + "\r\n";
                        }
                        res.setContenido(lineas);
                        res.setCodigo(true);
                        br.close();
                    } catch (IOException e) {
                        Log.d("ERROR", e.getMessage());
                        res.setCodigo(false);
                        res.setMensaje("ERROR HTTP: " + " error al leer el fichero " + file.getAbsolutePath() + " casua :" + e.getLocalizedMessage());
                    }

                } catch (FileNotFoundException e) {
                    Log.d("ERROR", e.getMessage());
                    res.setCodigo(false);
                    res.setMensaje("ERROR HTTP: " + " error al leer el fichero " + file.getAbsolutePath() + " causa :" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onStart() {
                super.onStart();
                /*progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progreso.setMessage("Conectando . . .");
                progreso.setCancelable(false);
                progreso.show();*/
            }
        });
        return res;
    }

    private String getFUrl() {
        return fUrl.getText().toString();
    }

    private String getImgUrl() {
        return imgUrl.getText().toString();
    }

    private boolean isFHTTP() {
        return fRadio.getCheckedRadioButtonId() == R.id.fHttp;
    }

    private boolean isIMGHttp() {
        return imgRadio.getCheckedRadioButtonId() == R.id.imgHttp;
    }

    public class SubirErrores extends AsyncTask<File, Integer, String> {

        private ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = new ProgressDialog(MainActivity.this);
            progreso.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progreso.setMessage("Subiendo errores . . .");
            progreso.setCancelable(true);
            progreso.show();
        }

        @Override
        protected String doInBackground(File... params) {
            if (conexionFtp.ftpConnect(HOST, PUERTO, USER, PASS)) {
                if (conexionFtp.ftpUpload(params[0], params[0].getName(), ".")) {
                    publishProgress(90);
                    return "Hubo errores de red, se ha subido el fichero de errores  a: " + "ftp://"+HOST+"/"+params[0].getName();
                }
                conexionFtp.ftpDisconnect();
                publishProgress(100);
            }
            return "Hubo errores de red, pero no se ha podido subir el fichero errores.txt al servidor ftp, asegura que el servidor ftp " + HOST + " existe y está operativo.";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progreso.dismiss();
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progreso.setProgress(values[0]);
        }
    }


    public class TareaAsincrona extends AsyncTask<String, Integer, Resultado[]> {
        private ProgressDialog progreso;

        protected void onPreExecute() {
            progreso = new ProgressDialog(MainActivity.this);
            progreso.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progreso.setMessage("Descargando . . .");
            progreso.setCancelable(true);
            progreso.show();
        }

        protected Resultado[] doInBackground(String... params) {
            Resultado[] resultado = new Resultado[2];

            if (params[0].equals("HTTP")) {
                resultado[0] = leerFicheroHttp(params[1]);
                if (resultado[0].getCodigo())
                    publishProgress(50);
            } else {
                HOST = Uri.parse(params[1]).getHost();
                DIRECTORIO = getDirectorio(params[1]);
                FICHERO = Uri.parse(params[1]).getLastPathSegment();

                if (conexionFtp.ftpConnect(HOST, PUERTO, USER, PASS)) {
                    if (conexionFtp.ftpDownload(FICHERO, DIRECTORIO, new File(getApplicationContext().getFilesDir(), FICHERO))) {
                        publishProgress(30);
                        resultado[0] = mem.leerInterna(FICHERO, COD);
                        if (resultado[0].getCodigo())
                            publishProgress(50);
                    } else {
                        resultado[0] = new Resultado();
                        resultado[0].setCodigo(false);
                        resultado[0].setMensaje("ERROR FTP: No se ha podido descargar el fichero " + FICHERO + " causa : " + "El fichero no existe");
                    }
                    conexionFtp.ftpDisconnect();
                } else {
                    resultado[0] = new Resultado();
                    resultado[0].setCodigo(false);
                    resultado[0].setMensaje("ERROR FTP: No se ha podido conectar al servidor " + HOST);
                }
            }
            if (params[2].equals("HTTP")) {
                resultado[1] = leerFicheroHttp(params[3]);
                if (resultado[1].getCodigo())
                    publishProgress(99);
            } else {
                HOST = Uri.parse(params[3]).getHost();
                DIRECTORIO = getDirectorio(params[3]);
                FICHERO = Uri.parse(params[3]).getLastPathSegment();

                if (conexionFtp.ftpConnect(HOST, PUERTO, USER, PASS)) {
                    if (conexionFtp.ftpDownload(FICHERO, DIRECTORIO, new File(getApplicationContext().getFilesDir(), FICHERO))) {
                        publishProgress(80);
                        resultado[1] = mem.leerInterna(FICHERO, COD);
                        if (resultado[1].getCodigo())
                            publishProgress(99);
                    } else {
                        resultado[1] = new Resultado();
                        resultado[1].setCodigo(false);
                        resultado[1].setMensaje("ERROR FTP: No se ha podido descargar el fichero " + FICHERO + " causa : " + "El fichero no existe");
                    }
                    conexionFtp.ftpDisconnect();
                } else {
                    resultado[1] = new Resultado();
                    resultado[1].setCodigo(false);
                    resultado[1].setMensaje("ERROR FTP: No se ha podido conectar al servidor " + HOST);
                }
            }
            return resultado;
        }

        protected void onPostExecute(Resultado[] resultado) {
            progreso.dismiss();
            if (resultado != null) {
                for (Resultado r : resultado) {
                    if (r != null) {
                        if (r.getCodigo()) {
                            //Toast.makeText(a, "Fichero leido correctamente ftp", Toast.LENGTH_SHORT).show();
                            String[] col = r.getContenido().split("\r\n");
                            for (int i = 0; i < col.length; i++) {
                                if (col[i].startsWith("http"))
                                    fotos.add(col[i]);
                                else
                                    frases.add(new String(col[i].getBytes(), Charset.forName(Charset.defaultCharset().name())));
                            }
                        } else
                            errores.add(df.format(new Date()) + " " + r.getMensaje());
                    }
                }


                int max = Math.max(fotos.size(), frases.size());
                int interval = _intervalo;
                if (max <= 0) {
                    interval = 1;
                }
                countDownTimer = new CountDownTimer(interval * 1000 * (max + 1), interval * 1000) {
                    int indice = 0;

                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (fotos.size() != 0) //division por 0
                            Picasso.with(MainActivity.this)
                                    .load(Uri.parse(fotos.get(indice % fotos.size())))
                                    .noPlaceholder()
                                    .fit()
                                    .error(R.drawable.error)
                                    .into(visor, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            //Toast.makeText(MainActivity.this, "Imagen descargada", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError() {
                                            errores.add(df.format(new Date()) + " error al descargar " + fotos.get(indice % fotos.size()) + " causa : " + "El fichero no existe");
                                            //Toast.makeText(MainActivity.this, "ERROR PICASSO: error al descargar imagen", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        if (frases.size() != 0)
                            textos.setText(frases.get(indice % frases.size()));
                        indice++;
                    }

                    @Override
                    public void onFinish() {
                        descargar.setEnabled(true);
                        indice = 0;

                        //Subir el fichero errores.txt
                        if (errores.size() > 0) {
                            File f = new File(getApplicationContext().getFilesDir(), "errores.txt");
                            for (String s : errores) {
                                boolean w = mem.escribirInterna(f.getName(), s + "\r\n", false, COD);
                            }

                            SubirErrores s = new SubirErrores();
                            s.execute(f);
                        }
                    }
                };
                countDownTimer.start();
            }
        }

        protected void onCancelled() {
            progreso.dismiss();
        }

        protected void onProgressUpdate(Integer... progress) {
            //progreso.setMessage("Descargando " + Integer.toString(progress[0]));
            progreso.setProgress(progress[0]);
        }
    }
}
