package app.clauncher.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.clauncher.data.AppModel

@Composable
fun AppItem(
    app: AppModel,
    onClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(app) }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = app.appLabel,
            modifier = Modifier.weight(1f)
        )
    }
}