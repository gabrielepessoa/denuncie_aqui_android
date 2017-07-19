package com.denucieaqui.android.gui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.denucieaqui.android.R;
import com.denucieaqui.android.dominio.Categoria;
import com.denucieaqui.android.dominio.Denuncia;
import com.denucieaqui.android.negocio.DenunciaNegocio;
import com.denucieaqui.android.service.CategoriaListService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Spinner spnCategoria, spnTipo;
    private Toolbar toolbar;
    private ProgressDialog load;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Infla Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spnCategoria = (Spinner) findViewById(R.id.spnCategoria);
        spnTipo = (Spinner) findViewById(R.id.spnTipo);

        carregarCategorias();

        //Pega categorias do ws e seta no spinner da activity
        spnCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                categoriaSelecionada(item);
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });

        /**
         * Finaliza o app
         */
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }


    /**
     * Cria o menu na activity
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapaDeCalor:
                Util.trocarTela(MainActivity.this, MapaDeCalorActivity.class);
                return true;
            case R.id.sobre:
                Util.trocarTela(MainActivity.this, SobreActivity.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Seta o tipo da categoria de acordo com a categoria escolhida
     * @param item
     */
    private void categoriaSelecionada(Object item) {
        String nomeCategoria = spnCategoria.getSelectedItem().toString();

        if (nomeCategoria.equals("Água e Energia")) {
            setArrayAdapterTipo(R.array.tipoAguaeEnergia);

        } else if (nomeCategoria.equals("Segurança")) {
            setArrayAdapterTipo(R.array.tipoSeguranca);
        }
    }

    /**
     * Cria um array e seta os tipos da categoria no spinner
     * @param i
     */
    private void setArrayAdapterTipo(int i) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(i));
        spnTipo.setAdapter(adapter);
    }

    /**
     * Validação de campo cateogoria e tipo e começo de construção do objeto denúncia
     * @param view
     */
    public void chamaDenunciaEndereco(View view) {

        if (spnCategoria.getSelectedItem().toString().trim() == "Categoria") {
            Util.showMsgToastShort(MainActivity.this, "Por favor, selecione a categoria.");
        } else if (spnTipo.getSelectedItem().toString().trim() == "") {
            Util.showMsgToastShort(MainActivity.this, "Por favor, selecione o tipo da categoria.");
        } else {
            Denuncia denuncia = new Denuncia();
            denuncia.setCategoriaDenuncia(spnCategoria.getSelectedItem().toString());
            denuncia.setTipoDenuncia(spnTipo.getSelectedItem().toString());
            DenunciaNegocio.setDenuncia(denuncia);
            Util.trocarTela(MainActivity.this, DenunciaEnderecoActivity.class);
        }
    }

    /**
     * Chama os métodos de comunicação com o web service
     */
    private void carregarCategorias() {
        TarefaDownload download = new TarefaDownload();
        Log.i("AsyncTask", "AsyncTask sendo chamada. Thread: " + Thread.currentThread().getName());
        download.execute();
    }


    /**
     * Comunicação com webservice e retorna uma lista de categorias
     */
    private class TarefaDownload extends AsyncTask<Void, Void, List<Categoria>> {

        /**
         * Retorno de dialog para o usuário saber o que o app está bloqueado enquanto há a requisição da categoria ao webservice
         */
        @Override
        protected void onPreExecute() {
            Log.i("AsyncTask", "Exibindo ProgressDialog na tela. Thread: " + Thread.currentThread().getName());
            load = ProgressDialog.show(MainActivity.this, "Por favor, aguarde ...",
                    "Carregando Categorias ...");
        }

        /**
         * Em paralelo ao dialog, este método chama método do service para comunicação com o ws
         * @param params
         * @return
         */
        @Override
        protected List<Categoria> doInBackground(Void... params) {
            Log.i("AsyncTask", "Requisição ao webservice. Thread: " + Thread.currentThread().getName());
            List<Categoria> listCategoria = CategoriaListService.carregarCategoria();
            return listCategoria;
        }

        /**
         * Pega a lista recebida do ws e seta no spinner de categoria
         * @param listaCategoriasWS
         */
        @Override
        protected void onPostExecute(List<Categoria> listaCategoriasWS) {

            List<String> listaCategorias = new ArrayList<String>();
            listaCategorias.add("Categoria");

            List<String> listaCategoriasValores = new ArrayList<String>();
            for (Categoria categoria : listaCategoriasWS) {
                listaCategoriasValores.add(categoria.getValue());
            }

            listaCategorias.addAll(listaCategoriasValores);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    listaCategorias);
            spnCategoria.setAdapter(adapter);

            Log.i("AsyncTask", "Tirando ProgressDialog da tela. Thread: " + Thread.currentThread().getName());
            load.dismiss();
        }
    }
}
