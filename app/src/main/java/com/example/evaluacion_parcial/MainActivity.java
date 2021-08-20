package com.example.evaluacion_parcial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback  {


    private GoogleMap gMaps;
    private ImageView textImg;
    private Button btnCargarImagen;
    private Button btnObtenerTexto;
    private TextView   nomPais;
    private TextView infoPais;
    private Uri imgPais;
    private ImageView bandera;

    private String codigo2;

    String urlApi;
    private RequestQueue requestQue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textImg=(ImageView)findViewById(R.id.imgTexto);
        btnCargarImagen=(Button)findViewById(R.id.btnCargarImagen);
        btnObtenerTexto=(Button)findViewById(R.id.btnCargarImagen);
        nomPais=(TextView)findViewById(R.id.txtPais);
        infoPais=(TextView)findViewById(R.id.txtInfo);
        infoPais.setMovementMethod(new ScrollingMovementMethod());
        bandera=(ImageView)findViewById(R.id.imgBandera);


        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMaps=googleMap;

        LatLng ltQuito= new LatLng(-0.270586, -78.408239);


        //Activar los controles del Zoom
        gMaps.getUiSettings().setZoomControlsEnabled(true);

        //Sin zoom
        CameraUpdate camUpdQuito= CameraUpdateFactory.newLatLngZoom(ltQuito,0);
        gMaps.moveCamera(camUpdQuito);
    }

    public void cargarUnaImagen(View view) {
        Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent,"Seleccione la aplicación"),10);

    }

    @Override
    protected void onActivityResult (int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            Uri imgUri =data.getData();
            imgPais=imgUri;
            textImg.setImageURI(imgUri);
        }
    }

    public void reconocerTexto(View view){
        if(imgPais!=null){
            InputImage ipImg;
            Uri uriImg=imgPais;
            TextRecognizer txtReconocedor;
            txtReconocedor=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            try{
                ipImg=InputImage.fromFilePath(this,uriImg);
                Task<Text> respuesta=txtReconocedor.process(ipImg).addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(@NonNull  Text text) {
                            nomPais.setText(text.getText());
                            System.out.println(text.getText());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        Toast.makeText(getApplicationContext(),"Error al intentar reconocer el texto: "
                                +e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Error al intentar reconocer el texto: "
                +e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }else{
            System.out.println("Debe seleccionar una imagen.");
        }
        consumirApiPaises();
    }

    public void consumirApiPaises(){
        urlApi="http://www.geognos.com/api/en/countries/info/all.json";
        JsonObjectRequest requestJsonObject=new JsonObjectRequest (Request.Method.GET, urlApi, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                            obtenerInfoPais(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error al intentar consumir la " +
                                "API:" + error.getMessage(), Toast.LENGTH_LONG).show();
                        System.out.println("eROROR: "+ error.getMessage());
                    }
                });
        requestQue = Volley.newRequestQueue(this);
        requestQue.add(requestJsonObject);
    }

    public void cargarImagen(){
        String urlImg= "http://www.geognos.com/api/en/countries/flag/"+codigo2+".png";

        Glide.with(this).load(urlImg).into(bandera);

    }


    public void obtenerInfoPais(JSONObject resp){
    try{
        JSONObject objetoGeneral=resp.getJSONObject("Results");
        //JSONArray arrayBD=objetoGeneral.getJSONArray("BD");
       //JSONObject objetoBD=objetoGeneral.getJSONObject("EC");
       // System.out.println("TAMAÑO DE RESULTS: "+arrayBD.length());
        //JSONArray arrayBD=objetoGeneral.getJSONArray(1);
        //System.out.println("CONTENIDO:"+arrayBD.get);
        JSONArray arrayPaises=objetoGeneral.toJSONArray(objetoGeneral.names());
        for (int i=0; i< arrayPaises.length();i++){
            //OBTENEMOS EL PAIS
            JSONObject objetoInfo=arrayPaises.getJSONObject(i);

            String nomAuxPais= objetoInfo.get("Name").toString().toUpperCase();

            if(nomAuxPais.equals(nomPais.getText().toString().toUpperCase())) {

                //PARA OBTENER LA CAPITAL
                JSONObject objetoCapital = objetoInfo.getJSONObject("Capital");
                //PARA OBTENER LOS CODIGOS DE ESE PAIS
                JSONObject objetoCodigo = objetoInfo.getJSONObject("CountryCodes");
                //PARA OBTENER EL CENTRO
                JSONArray arrayCentro=objetoInfo.getJSONArray("GeoPt");
                //PARA OBTENER EL RECTANGULO
                JSONObject objetoRectangulo=objetoInfo.getJSONObject("GeoRectangle");

                infoPais.setText("");
                //CARGAMOS LA CAPITAL
                infoPais.append("Capital:   " + objetoCapital.get("Name")+"\n");

                //CARGAMOS LOS CODIGOS
                infoPais.append("Code ISO 2:   " + objetoCodigo.get("iso2")+"\n");
                infoPais.append("Code ISO Num:   " + objetoCodigo.get("isoN")+"\n");
                infoPais.append("Code ISO 3:   " + objetoCodigo.get("iso3")+"\n");
                infoPais.append("Code ISO FIPS:   " + objetoCodigo.get("fips")+"\n");

                //CARGAMOS EL CODIGO 2 A LA VARIABLE GLOBAL (PARA QUE CARGUE LA IMAGEN)
                codigo2=objetoCodigo.get("iso2").toString();


                //CARGAMOS EL PREFIJO DEL TELEFONO
                infoPais.append("Tel Prefix: " + objetoInfo.get("TelPref")+"\n");

                //CARGAMOS EL CENTRO
                infoPais.append("Center°:   "+arrayCentro.get(0).toString()
                        +" "+arrayCentro.get(1).toString() +"\n");

                //CARGAMOS EL RECTANGULO
                infoPais.append("Rectangle°: \n"+objetoRectangulo.get("West")
                        +"\n "+objetoRectangulo.get("North")
                        +" \n"+objetoRectangulo.get("East")
                        +"\n "+objetoRectangulo.get("South"));

                //CARGAMOS LAS LATITUDES
                double lt1,lt2;
                lt1=Double.valueOf(arrayCentro.get(0).toString());
                lt2=Double.valueOf(arrayCentro.get(1).toString());

                //CARGAMOS LOS PUNTOS PARA LOS RECTANGULOS
                double pnt1,pnt2,pnt3,pnt4;
                double[] puntos={0,0,0,0};

                puntos[0]=Double.valueOf(objetoRectangulo.get("West").toString());
                puntos[1]=Double.valueOf(objetoRectangulo.get("North").toString());
                puntos[2]=Double.valueOf(objetoRectangulo.get("East").toString());
                puntos[3]=Double.valueOf(objetoRectangulo.get("South").toString());

                //PROCEDIMIENTO PARA HACER ZOOM Y GENERAR UN CUADRO EN EL PAIS
                zoomYRectanguloPais(lt1,lt2,puntos);

                if(codigo2!=null){
                    cargarImagen();
                }
            }
        }

    }catch (JSONException e){
        System.out.println("Error: "+e.getMessage());
    }
    }
                        //1 y 3 son y
    public void zoomYRectanguloPais(double v, double v1, double[] pts){
        LatLng ltPais= new LatLng(v, v1);
        //Para colocar el rectangulo
        PolylineOptions rectangulo=new PolylineOptions()
                .add(new LatLng(pts[3],pts[0]))
                .add(new LatLng(pts[1],pts[0]))
                .add(new LatLng(pts[1],pts[2]))
                .add(new LatLng(pts[3],pts[2]))
                .add(new LatLng(pts[3],pts[0]));
        rectangulo.width(12);
        rectangulo.color(Color.BLUE);
        gMaps.addPolyline(rectangulo);

        //Zoom en el pais
        CameraUpdate camUpdPais= CameraUpdateFactory.newLatLngZoom(ltPais,4);
        gMaps.moveCamera(camUpdPais);


    }


}