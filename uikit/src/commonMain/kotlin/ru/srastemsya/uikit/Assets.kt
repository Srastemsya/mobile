package ru.srastemsya.uikit

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.srastemsya.uikit.resources.Res
import ru.srastemsya.uikit.resources.home_bar_cover
import ru.srastemsya.uikit.resources.home_russky_cover
import ru.srastemsya.uikit.resources.swipe_accept_ticket

enum class SrIcon(internal val file: String, internal val naturalSize: Int) {
    Home("f8380_imghome", 22), Map("f8380_imgpin", 22), Feed("f8380_img", 22),
    Events("f8380_imgcalendar", 22), Profile("f8380_imguser", 22),
    Filter("f8487_imgfilter", 21), Location("f8490_imgtarget", 21),
    ZoomIn("f8493_imgplus", 21), ZoomOut("f8496_imgminus", 21),
    Back("f8499_imgback", 21), Close("f8502_imgclose", 21), Save("f8505_imgbookmark", 21),
    Share("f8508_imgshare", 21), More("f9096_imgdots", 20), Bell("f9096_imgbell", 20),
    Next("f9140_imgnext", 16), Expand("f8585_imgchevrondown", 20), Search("f9194_imgsearch", 24),
    Clock("f9194_imgclock", 24), Warning("f9194_imgalert", 24), Check("f9244_imgcheck", 18),
    Plus("f9244_imgplus", 18), People("f9255_imgusers", 24), Link("f9570_imglink", 24),
    Spark("f9445_imgspark", 24), Undo("f9444_imgundo", 24), Heart("f9444_imgheart", 24),
    Photo("f9466_imgphoto", 24), Star("f9607_imgstar", 20), StarOutline("f9607_imgstar1", 20)
}

@Composable
fun SrGlyph(
    icon: SrIcon,
    modifier: Modifier = Modifier,
    description: String? = null,
    tint: Color = SrColors.Muted,
    size: Dp = icon.naturalSize.dp
) = FigmaSvg(icon.file, modifier.size(size), description, tint)

enum class SrIllustration(internal val file: String) {
    Indoor("f9466_img"), Outdoor("f9466_img1"), GlassDemo("f9078_img1"),
    VoteCompact("f9290_img"), VoteTall("f9290_img1"), VoteFull("f9290_img2"),
    EventThumbnail("f9846_img")
}

@Composable
fun SrArtwork(artwork: SrIllustration, modifier: Modifier = Modifier, description: String? = null) =
    FigmaSvg(artwork.file, modifier, description, scale = ContentScale.Crop)

enum class SrHomeVector(internal val file: String) {
    Brand("home_logo"), Bay("home_bay"), Bell("home_bell"), Spark("home_spark"),
    SwipeArt("home_swipe_art"), SwipeSpark("home_swipe_spark"), Compass("home_compass"),
    Next("home_next"), Chevron("home_chevron"), Heart("home_heart"), HeartFilled("home_heart_filled")
}

enum class SrHomeCover { SvetlanskayaBars, RusskyWeekend }

@Composable
fun SrHomeVectorImage(
    vector: SrHomeVector,
    modifier: Modifier = Modifier,
    description: String? = null,
    tint: Color? = null,
) = FigmaSvg(vector.file, modifier, description, tint)

@Composable
fun SrHomeCoverImage(cover: SrHomeCover, modifier: Modifier = Modifier, description: String? = null) {
    val painter = when (cover) {
        SrHomeCover.SvetlanskayaBars -> painterResource(Res.drawable.home_bar_cover)
        SrHomeCover.RusskyWeekend -> painterResource(Res.drawable.home_russky_cover)
    }
    Image(painter, description, modifier, contentScale = ContentScale.Crop)
}

@Composable
fun SrSwipeAcceptImage(modifier: Modifier = Modifier, description: String = "Да") {
    Image(
        painterResource(Res.drawable.swipe_accept_ticket),
        description,
        modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
internal expect fun FigmaSvg(
    file: String, modifier: Modifier, description: String? = null,
    tint: Color? = null, scale: ContentScale = ContentScale.Fit
)
