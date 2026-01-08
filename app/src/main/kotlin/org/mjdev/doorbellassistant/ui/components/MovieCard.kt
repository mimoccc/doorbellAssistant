//package org.mjdev.doorbellassistant.ui.components
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.drawWithCache
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import org.mjdev.doorbellassistant.ui.theme.DoorBellAssistantTheme
//import org.mjdev.phone.extensions.CustomExtensions.rememberAssetImage
//import org.mjdev.phone.helpers.Previews
//
//@Previews
//@Composable
//fun MovieCard(
//    modifier: Modifier = Modifier,
//    bitmap: ImageBitmap = rememberAssetImage("avatar_transparent.png"),
//    title: String = "title",
//    subtitle: String = "subtitle",
//) = DoorBellAssistantTheme {
//    Card(
//        modifier = modifier.size(width = 140.dp, height = 180.dp),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Box {
//            Image(
//                bitmap = bitmap,
//                contentDescription = title,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
//            Spacer(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .drawWithCache {
//                        val gradient = Brush.verticalGradient(
//                            colorStops = arrayOf(
//                                0.0f to Color.Transparent,
//                                0.5f to Color.Transparent,
//                                0.75f to Color.Black.copy(alpha = 0.5f),
//                                1.0f to Color.Black.copy(alpha = 0.9f)
//                            )
//                        )
//                        onDrawBehind {
//                            drawRect(gradient)
//                        }
//                    }
//            )
//            Column(
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(12.dp)
//            ) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.titleSmall,
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold
//                )
//                Text(
//                    text = subtitle,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.White.copy(alpha = 0.7f)
//                )
//            }
//        }
//    }
//}
