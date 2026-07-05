package com.pctracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A specific product page to scrape for a given component at a given provider. Users add
 * these themselves (Settings > Liens produits) since matching the exact SKU on each site
 * cannot be done reliably by automated search.
 */
@Entity(
    tableName = "product_links",
    foreignKeys = [
        ForeignKey(
            entity = ComponentEntity::class,
            parentColumns = ["id"],
            childColumns = ["componentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("componentId"), Index("provider")]
)
data class ProductLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val componentId: Long,
    val provider: String,
    val url: String,
    val label: String? = null,
    val enabled: Boolean = true
)
