package us.ait.artlocater.Data

import com.google.android.gms.maps.model.Marker

data class Post(
    var uid: String = "",
    var author: String = "",
    var imageURL: String = "",
    var marker : Marker? = null
)