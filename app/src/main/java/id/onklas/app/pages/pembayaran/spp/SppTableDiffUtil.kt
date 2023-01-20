package id.onklas.app.pages.pembayaran.spp

import androidx.recyclerview.widget.DiffUtil
import javax.inject.Inject

class SppTableDiffUtil @Inject constructor() : DiffUtil.ItemCallback<SppTable>() {
    override fun areItemsTheSame(oldItem: SppTable, newItem: SppTable): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SppTable, newItem: SppTable): Boolean =
        oldItem == newItem
}