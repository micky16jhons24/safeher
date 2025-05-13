package com.example.safeher20;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.safeher20.ChatMessage;

public class AudioViewHolder extends RecyclerView.ViewHolder{
    ImageView playButton;
    TextView timeText;
    MediaPlayer mediaPlayer;

    public AudioViewHolder(View itemView) {
        super(itemView);
        playButton = itemView.findViewById(R.id.play_button);
        timeText = itemView.findViewById(R.id.audio_time);
//
        playButton.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(itemView.getContext(), R.raw.nota_voz);
                mediaPlayer.setOnCompletionListener(mp -> {
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                    mediaPlayer.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                playButton.setImageResource(android.R.drawable.ic_media_play);
            }
        });
    }

    public void bind(ChatMessage message) {
        timeText.setText(message.getTime());
    }
}
