package com.example.pavewatchapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    // Bandera para saber si mostramos 3 o 4 pestañas
    private boolean isDevModeActive = false;

    public ViewPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    // Método para activar/desactivar el menú oculto
    public void toggleDevMode(boolean active) {
        this.isDevModeActive = active;
        notifyDataSetChanged(); // Obliga al ViewPager a refrescarse
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (!isDevModeActive) {
            // MODO NORMAL: 3 Pestañas
            switch (position) {
                case 0: return HistorialFragment.newInstance();
                case 1: return MapaFragment.newInstance();
                case 2: return new EstadisticasFragment();
                default: return HistorialFragment.newInstance();
            }
        } else {
            // MODO DESARROLLADOR: 4 Pestañas
            switch (position) {
                case 0: return HistorialFragment.newInstance();
                case 1: return MapaFragment.newInstance();
                case 2: return MonitoreoFragment.newInstance(); // ¡Aparece el oculto!
                case 3: return new EstadisticasFragment();
                default: return HistorialFragment.newInstance();
            }
        }
    }

    @Override
    public int getItemCount() {
        return isDevModeActive ? 4 : 3;
    }
}