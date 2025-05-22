package com.example.safeher20;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class ChatActivity  extends AppCompatActivity {

    RecyclerView recyclerView;
    ChatAdapter adapter;
    List<ChatMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
