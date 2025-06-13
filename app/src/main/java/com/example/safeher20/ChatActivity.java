package com.example.safeher20;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ChatAdapter adapter;
    List<ChatMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v ->finish());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.Cabecera));
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            // Obtener altura de la barra de estado
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }

            // Agregar padding top a la cabecera para que el contenido no quede debajo de la barra
            LinearLayout chatHeader = findViewById(R.id.chat_header);
            chatHeader.setPadding(
                    chatHeader.getPaddingLeft(),
                    statusBarHeight,
                    chatHeader.getPaddingRight(),
                    chatHeader.getPaddingBottom()
            );
        }



        messages = new ArrayList<>();
        messages.add(new ChatMessage("Neni, como tienes la tarde? tengo que ir con tu abuelo al medico.", "19:08", ChatMessage.TYPE_LEFT));
        messages.add(new ChatMessage("Hoy tengo planes, no sé lo que tardaré en llegar a casa, necesitas que te lleve?", "19:09", ChatMessage.TYPE_RIGHT));
        messages.add(new ChatMessage("No, vamos en el autobús pero estate pendiente del teléfono, vale amor?", "19:10", ChatMessage.TYPE_LEFT));
        messages.add(new ChatMessage("Vale mami, me vas diciendo, es solo consulta? O le van a hacer algo al final?", "19:10", ChatMessage.TYPE_RIGHT));
        messages.add(new ChatMessage("Pues en teoría es solo consulta pero como le molestaba hoy, no sabremos qué pasará...", "19:11", ChatMessage.TYPE_LEFT));
        messages.add(new ChatMessage("Okii, con lo que sea me llamas, vale? Un besito", "19:12", ChatMessage.TYPE_RIGHT));
        messages.add(new ChatMessage("", "19:12", ChatMessage.TYPE_AUDIO));


        adapter = new ChatAdapter(messages);
        recyclerView.setAdapter(adapter);
    }
}
