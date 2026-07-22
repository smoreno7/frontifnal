package com.example.pavewatchapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    // --- Variables del Menú Oculto ---
    private String[] tabTitles = {"HISTORIAL", "MAPA", "ESTADÍSTICAS"};
    private boolean isDevMode = false;
    private int tapCount = 0;
    private long lastTapTime = 0;
    private ViewPagerAdapter adapter;
    private TabLayoutMediator mediator;

    // Variable global del botón sensor
    private boolean isSensorActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configurarNavegacion();
        crearCanalNotificaciones();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // --- CONFIGURACIÓN DEL BOTÓN FLOTANTE ---
        FloatingActionButton fabSensorToggle = findViewById(R.id.fab_sensor_toggle);
        iniciarDeteccionBaches();

        fabSensorToggle.setOnClickListener(v -> {
            if (isSensorActive) {
                detenerDeteccionBaches();
                isSensorActive = false;
                fabSensorToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                Toast.makeText(this, "Sensor automático desactivado", Toast.LENGTH_SHORT).show();
            } else {
                iniciarDeteccionBaches();
                isSensorActive = true;
                fabSensorToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                Toast.makeText(this, "Sensor automático activado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarNavegacion() {
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);
        viewPager2.setCurrentItem(1, false);

        // Inicializar el mediador por primera vez
        actualizarTabs(tabLayout, viewPager2);

        // Lógica de los 5 toques (Easter Egg)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Si toca cualquier otra pestaña, reiniciamos el contador
                if (tab.getPosition() != 0) {
                    tapCount = 0;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // onTabReselected se dispara cuando vuelves a tocar la pestaña en la que ya estás
                if (tab.getPosition() == 0) { // 0 es HISTORIAL
                    long currentTime = System.currentTimeMillis();
                    // Si el toque fue rápido (menos de 500ms desde el anterior)
                    if (currentTime - lastTapTime < 500) {
                        tapCount++;
                        if (tapCount >= 5) { // Al llegar a 5 toques rápidos
                            isDevMode = !isDevMode;

                            if (isDevMode) {
                                tabTitles = new String[]{"HISTORIAL", "MAPA", "MONITOREO", "ESTADÍSTICAS"};
                                Toast.makeText(MainActivity.this, "Panel de Monitoreo Revelado", Toast.LENGTH_SHORT).show();
                            } else {
                                tabTitles = new String[]{"HISTORIAL", "MAPA", "ESTADÍSTICAS"};
                                Toast.makeText(MainActivity.this, "Panel de Monitoreo Oculto", Toast.LENGTH_SHORT).show();
                            }

                            adapter.toggleDevMode(isDevMode);
                            actualizarTabs(tabLayout, viewPager2);
                            tapCount = 0; // Reiniciar contador
                        }
                    } else {
                        // Si se demoró mucho entre toques, reiniciar a 1
                        tapCount = 1;
                    }
                    lastTapTime = currentTime;
                }
            }
        });
    }

    // Método para reconstruir los títulos de las pestañas dinámicamente
    private void actualizarTabs(TabLayout tabLayout, ViewPager2 viewPager2) {
        if (mediator != null) {
            mediator.detach(); // Desconectar el viejo
        }
        mediator = new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(tabTitles[position])
        );
        mediator.attach(); // Conectar el nuevo con los títulos actualizados
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "CanalMonitoreo",
                    "Monitoreo de Baches",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    private void iniciarDeteccionBaches() {
        Intent serviceIntent = new Intent(this, MonitoreoService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void detenerDeteccionBaches() {
        Intent serviceIntent = new Intent(this, MonitoreoService.class);
        stopService(serviceIntent);
    }
}