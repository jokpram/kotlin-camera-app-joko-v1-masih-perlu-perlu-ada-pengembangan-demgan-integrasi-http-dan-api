package com.joko.cameraapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joko.cameraapp.R
import com.joko.cameraapp.utils.ImageProcessor

class EffectsAdapter(
    private val effects: List<ImageProcessor.Effect>,
    private val onEffectSelected: (ImageProcessor.Effect) -> Unit
) : RecyclerView.Adapter<EffectsAdapter.EffectViewHolder>() {

    private var selectedPosition = 0

    class EffectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val effectName: TextView = itemView.findViewById(R.id.tvEffectName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_effect, parent, false)
        return EffectViewHolder(view)
    }

    override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
        val effect = effects[position]
        holder.effectName.text = when (effect) {
            ImageProcessor.Effect.NONE -> "Normal"
            ImageProcessor.Effect.DUOTONE_RED_BLUE -> "Duotone RB"
            ImageProcessor.Effect.DUOTONE_GREEN_PURPLE -> "Duotone GP"
            ImageProcessor.Effect.GRAIN_NOISE -> "Grain"
            ImageProcessor.Effect.DUOTONE_WITH_GRAIN -> "Duotone+Grain"
            ImageProcessor.Effect.VINTAGE_SEPIA -> "Sepia"
            ImageProcessor.Effect.COLD_BLUE -> "Cold Blue"
            ImageProcessor.Effect.WARM_AMBER -> "Warm Amber"
        }

        // Update background based on selection
        if (position == selectedPosition) {
            holder.effectName.setBackgroundResource(R.drawable.effect_background_selected)
            holder.effectName.setTextColor(holder.itemView.context.getColor(android.R.color.black))
        } else {
            holder.effectName.setBackgroundResource(R.drawable.effect_background)
            holder.effectName.setTextColor(holder.itemView.context.getColor(android.R.color.white))
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)
            onEffectSelected(effect)
        }
    }

    override fun getItemCount(): Int = effects.size
}