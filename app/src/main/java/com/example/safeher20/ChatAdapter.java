package com.example.safeher20;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == ChatMessage.TYPE_LEFT) {
            view = inflater.inflate(R.layout.item_chat_left, parent, false);
            return new LeftViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_RIGHT) {
            view = inflater.inflate(R.layout.item_chat_right, parent, false);
            return new RightViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_AUDIO) {
            view = inflater.inflate(R.layout.item_chat_audio_left, parent, false);
            return new AudioViewHolder(view, parent.getContext());
        }

        throw new IllegalArgumentException("Tipo de vista desconocido: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof LeftViewHolder) {
            ((LeftViewHolder) holder).messageText.setText(message.getMessage());
            ((LeftViewHolder) holder).timeText.setText(message.getTime());
        } else if (holder instanceof RightViewHolder) {
            ((RightViewHolder) holder).messageText.setText(message.getMessage());
            ((RightViewHolder) holder).timeText.setText(message.getTime());
        } else if (holder instanceof AudioViewHolder) {
            ((AudioViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        LeftViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
        }
    }

    static class RightViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        RightViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
        }
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        ImageView playButton;
        TextView timeText;
        MediaPlayer mediaPlayer;
        Context context;
        boolean isPlaying = false;

        AudioViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            playButton = itemView.findViewById(R.id.play_button);
            timeText = itemView.findViewById(R.id.audio_time);

            playButton.setOnClickListener(v -> {
                if (!isPlaying) {
                    mediaPlayer = MediaPlayer.create(context, R.raw.nota_voz);
                    mediaPlayer.setOnCompletionListener(mp -> {
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                        isPlaying = false;
                        mediaPlayer.release();
                        mediaPlayer = null;
                    });
                    mediaPlayer.start();
                    isPlaying = true;
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    isPlaying = false;
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                }
            });
        }

        public void bind(ChatMessage message) {
            timeText.setText(message.getTime());
        }
    }
}