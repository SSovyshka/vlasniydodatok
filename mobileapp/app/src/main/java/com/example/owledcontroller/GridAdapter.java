package com.example.owledcontroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

public class GridAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> flowerName;
    ArrayList<Integer> image;

    LayoutInflater inflater;

    public GridAdapter(Context context, ArrayList<String> flowerName, ArrayList<Integer> image) {
        this.context = context;
        this.flowerName = flowerName;
        this.image = image;
    }

    @Override
    public int getCount() {
        return flowerName.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Если View не создана, создаем ее из макета grid_item
        if (view == null)
            view = inflater.inflate(R.layout.grid_item, null);

        // Находим элементы ImageView и TextView в макете grid_item
        GifImageView imageView = view.findViewById(R.id.item_image);
        TextView textView = view.findViewById(R.id.item_text);

        // Устанавливаем изображение и текст для текущего элемента
        imageView.setImageResource(image.get(i));
        textView.setText(flowerName.get(i));

        // Возвращаем View, представляющую текущий элемент в GridView
        return view;
    }
}