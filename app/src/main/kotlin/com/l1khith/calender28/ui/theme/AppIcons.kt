package com.l1khith.calender28.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

object AppIcons {
    val AppLogo: ImageVector
        @Composable get() = remember {
            ImageVector.Builder(
                name = "AppLogo",
                defaultWidth = 108.dp, defaultHeight = 108.dp,
                viewportWidth = 108f, viewportHeight = 108f
            ).addPath(
                pathData = PathParser().parsePathString("M0 0h108v108H0z").toNodes(),
                fill = SolidColor(Color(0xFF121212))
            ).addPath(
                pathData = PathParser().parsePathString("M32 24h40c2.2 0 4 1.8 4 4v12L64 28H32z").toNodes(),
                fill = SolidColor(Color(0xFF2563EB))
            ).addPath(
                pathData = PathParser().parsePathString("M32 28h32l12 12v40c0 2.2-1.8 4-4 4H36c-2.2 0-4-1.8-4-4V28z").toNodes(),
                fill = SolidColor(Color(0xFF3B82F6))
            ).addPath(
                pathData = PathParser().parsePathString("M64 28l12 12h-8c-2.2 0-4-1.8-4-4v-8z").toNodes(),
                fill = SolidColor(Color(0xFF1D4ED8))
            ).addPath(
                pathData = PathParser().parsePathString("M43 51c0-2.5 2-4 4.5-4s4.5 1.5 4.5 4c0 2.5-2.5 5-9 11v3h10v-3.5h-5.5l3-3c2.5-2.5 3.5-5 3.5-7.5 0-4-3-6.5-6.5-6.5S41 47 41 51h2z").toNodes(),
                fill = SolidColor(Color.White)
            ).addPath(
                pathData = PathParser().parsePathString("M60.5 44.5c-3.5 0-5.5 2-5.5 4.5 0 2 1.5 3.5 3 4.5-2 1-4 2.5-4 5.5 0 3 2.5 6 6.5 6s6.5-3 6.5-6c0-3-2-4.5-4-5.5 1.5-1 3-2.5 3-4.5 0-2.5-2-4.5-5.5-4.5zm0 2.5c1.5 0 3 1 3 2.5s-1.5 2.5-3 2.5-3-1-3-2.5 1.5-2.5 3-2.5zm0 7.5c2 0 4 1.5 4 4.5s-2 3.5-4 3.5-4-.5-4-3.5 2-4.5 4-4.5z").toNodes(),
                fill = SolidColor(Color.White)
            ).build()
        }

    val Subscription: ImageVector

        @Composable get() = remember {
            ImageVector.Builder(
                name = "SubscriptionIcon",
                defaultWidth = 20.dp, defaultHeight = 16.dp,
                viewportWidth = 20f, viewportHeight = 16f
            ).addPath(
                pathData = PathParser().parsePathString(
                    "M20 2V14C20 14.55 19.8042 15.0208 19.4125 15.4125C19.0208 15.8042 18.55 16 18 16H2C1.45 16 0.979167 15.8042 0.5875 15.4125C0.195833 15.0208 0 14.55 0 14V2C0 1.45 0.195833 0.979167 0.5875 0.5875C0.979167 0.195833 1.45 0 2 0H18C18.55 0 19.0208 0.195833 19.4125 0.5875C19.8042 0.979167 20 1.45 20 2V2M2 4H18V2V2V2H2V2V2V4V4M2 8V14V14V14H18V14V14V8H2V8M2 14V14V14V14V2V2V2V2V2V2V14V14V14V14"
                ).toNodes(),
                fill = SolidColor(Color(0xFFC2C6D6))
            ).build()
        }

    val Security: ImageVector
        @Composable get() = remember {
            ImageVector.Builder(
                name = "SecurityIcon",
                defaultWidth = 16.dp, defaultHeight = 20.dp,
                viewportWidth = 16f, viewportHeight = 20f
            ).addPath(
                pathData = PathParser().parsePathString(
                    "M8 20C5.68333 19.4167 3.77083 18.0875 2.2625 16.0125C0.754167 13.9375 0 11.6333 0 9.1V3L8 0L16 3V9.1C16 11.6333 15.2458 13.9375 13.7375 16.0125C12.2292 18.0875 10.3167 19.4167 8 20ZM8 17.9C9.61667 17.4 10.9667 16.4125 12.05 14.9375C13.1333 13.4625 13.7667 11.8167 13.95 10H8V2.125L2 4.375V9.1C2 9.28333 2 9.43333 2 9.55C2 9.66667 2.01667 9.81667 2.05 10H8V17.9Z"
                ).toNodes(),
                fill = SolidColor(Color(0xFFC2C6D6))
            ).build()
        }

    val Notification: ImageVector
        @Composable get() = remember {
            ImageVector.Builder(
                name = "NotificationIcon",
                defaultWidth = 16.dp, defaultHeight = 20.dp,
                viewportWidth = 16f, viewportHeight = 20f
            ).addPath(
                pathData = PathParser().parsePathString(
                    "M0 17V15H2V8C2 6.61667 2.41667 5.3875 3.25 4.3125C4.08333 3.2375 5.16667 2.53333 6.5 2.2V1.5C6.5 1.08333 6.64583 0.729167 6.9375 0.4375C7.22917 0.145833 7.58333 0 8 0C8.41667 0 8.77083 0.145833 9.0625 0.4375C9.35417 0.729167 9.5 1.08333 9.5 1.5V2.2C10.8333 2.53333 11.9167 3.2375 12.75 4.3125C13.5833 5.3875 14 6.61667 14 8V15H16V17H0ZM8 20C7.45 20 6.97917 19.8042 6.5875 19.4125C6.19583 19.0208 6 18.55 6 18H10C10 18.55 9.80417 19.0208 9.4125 19.4125C9.02083 19.8042 8.55 20 8 20ZM4 15H12V8C12 6.9 11.6083 5.95833 10.825 5.175C10.0417 4.39167 9.1 4 8 4C6.9 4 5.95833 4.39167 5.175 5.175C4.39167 5.95833 4 6.9 4 8V15Z"
                ).toNodes(),
                fill = SolidColor(Color(0xFFC2C6D6))
            ).build()
        }

    val Appearance: ImageVector
        @Composable get() = remember {
            ImageVector.Builder(
                name = "AppearanceIcon",
                defaultWidth = 20.dp, defaultHeight = 20.dp,
                viewportWidth = 20f, viewportHeight = 20f
            ).addPath(
                pathData = PathParser().parsePathString(
                    "M10 20C8.63333 20 7.34167 19.7375 6.125 19.2125C4.90833 18.6875 3.84583 17.9708 2.9375 17.0625C2.02917 16.1542 1.3125 15.0917 0.7875 13.875C0.2625 12.6583 0 11.3667 0 10C0 8.61667 0.270833 7.31667 0.8125 6.1C1.35417 4.88333 2.0875 3.825 3.0125 2.925C3.9375 2.025 5.01667 1.3125 6.25 0.7875C7.48333 0.2625 8.8 0 10.2 0C11.5333 0 12.7917 0.229167 13.975 0.6875C15.1583 1.14583 16.1958 1.77917 17.0875 2.5875C17.9792 3.39583 18.6875 4.35417 19.2125 5.4625C19.7375 6.57083 20 7.76667 20 9.05C20 10.9667 19.4167 12.4375 18.25 13.4625C17.0833 14.4875 15.6667 15 14 15H12.15C12 15 11.8958 15.0417 11.8375 15.125C11.7792 15.2083 11.75 15.3 11.75 15.4C11.75 15.6 11.875 15.8875 12.125 16.2625C12.375 16.6375 12.5 17.0667 12.5 17.55C12.5 18.3833 12.2708 19 11.8125 19.4C11.3542 19.8 10.75 20 10 20ZM4.5 11C4.93333 11 5.29167 10.8583 5.575 10.575C5.85833 10.2917 6 9.93333 6 9.5C6 9.06667 5.85833 8.70833 5.575 8.425C5.29167 8.14167 4.93333 8 4.5 8C4.06667 8 3.70833 8.14167 3.425 8.425C3.14167 8.70833 3 9.06667 3 9.5C3 9.93333 3.14167 10.2917 3.425 10.575C3.70833 10.8583 4.06667 11 4.5 11ZM7.5 7C7.93333 7 8.29167 6.85833 8.575 6.575C8.85833 6.29167 9 5.93333 9 5.5C9 5.06667 8.85833 4.70833 8.575 4.425C8.29167 4.14167 7.93333 4 7.5 4C7.06667 4 6.70833 4.14167 6.425 4.425C6.14167 4.70833 6 5.06667 6 5.5C6 5.93333 6.14167 6.29167 6.425 6.575C6.70833 6.85833 7.06667 7 7.5 7ZM12.5 7C12.9333 7 13.2917 6.85833 13.575 6.575C13.8583 6.29167 14 5.93333 14 5.5C14 5.06667 13.8583 4.70833 13.575 4.425C13.2917 4.14167 12.9333 4 12.5 4C12.0667 4 11.7083 4.14167 11.425 4.425C11.1417 4.70833 11 5.06667 11 5.5C11 5.93333 11.1417 6.29167 11.425 6.575C11.7083 6.85833 12.0667 7 12.5 7ZM15.5 11C15.9333 11 16.2917 10.8583 16.575 10.575C16.8583 10.2917 17 9.93333 17 9.5C17 9.06667 16.8583 8.70833 16.575 8.425C16.2917 8.14167 15.9333 8 15.5 8C15.0667 8 14.7083 8.14167 14.425 8.425C14.1417 8.70833 14 9.06667 14 9.5C14 9.93333 14.1417 10.2917 14.425 10.575C14.7083 10.8583 15.0667 11 15.5 11ZM10 18C10.15 18 10.2708 17.9583 10.3625 17.875C10.4542 17.7917 10.5 17.6833 10.5 17.55C10.5 17.3167 10.375 17.0417 10.125 16.725C9.875 16.4083 9.75 15.9333 9.75 15.3C9.75 14.6 9.99167 14.0417 10.475 13.625C10.9583 13.2083 11.55 13 12.25 13H14C15.1 13 16.0417 12.6792 16.825 12.0375C17.6083 11.3958 18 10.4 18 9.05C18 7.03333 17.2292 5.35417 15.6875 4.0125C14.1458 2.67083 12.3167 2 10.2 2C7.93333 2 6 2.775 4.4 4.325C2.8 5.875 2 7.76667 2 10C2 12.2167 2.77917 14.1042 4.3375 15.6625C5.89583 17.2208 7.78333 18 10 18Z"
                ).toNodes(),
                fill = SolidColor(Color(0xFFC2C6D6))
            ).build()
        }

    val ReminderTime: ImageVector
        @Composable get() = remember {
            ImageVector.Builder(
                name = "ReminderIcon",
                defaultWidth = 20.dp, defaultHeight = 20.dp,
                viewportWidth = 20f, viewportHeight = 20f
            ).addPath(
                pathData = PathParser().parsePathString(
                    "M13.3 14.7L14.7 13.3L11 9.6V5H9V10.4L13.3 14.7V14.7M10 20C8.61667 20 7.31667 19.7375 6.1 19.2125C4.88333 18.6875 3.825 17.975 2.925 17.075C2.025 16.175 1.3125 15.1167 0.7875 13.9C0.2625 12.6833 0 11.3833 0 10C0 8.61667 0.2625 7.31667 0.7875 6.1C1.3125 4.88333 2.025 3.825 2.925 2.925C3.825 2.025 4.88333 1.3125 6.1 0.7875C7.31667 0.2625 8.61667 0 10 0C11.3833 0 12.6833 0.2625 13.9 0.7875C15.1167 1.3125 16.175 2.025 17.075 2.925C17.975 3.825 18.6875 4.88333 19.2125 6.1C19.7375 7.31667 20 8.61667 20 10C20 11.3833 19.7375 12.6833 19.2125 13.9C18.6875 15.1167 17.975 16.175 17.075 17.075C16.175 17.975 15.1167 18.6875 13.9 19.2125C12.6833 19.7375 11.3833 20 10 20V20M10 10V10V10V10V10V10V10V10V10V10M10 18C12.2167 18 14.1042 17.2208 15.6625 15.6625C17.2208 14.1042 18 12.2167 18 10C18 7.78333 17.2208 5.89583 15.6625 4.3375C14.1042 2.77917 12.2167 2 10 2C7.78333 2 5.89583 2.77917 4.3375 4.3375C2.77917 5.89583 2 7.78333 2 10C2 12.2167 2.77917 14.1042 4.3375 15.6625C5.89583 17.2208 7.78333 18 10 18V18"
                ).toNodes(),
                fill = SolidColor(Color(0xFFC2C6D6))
            ).build()
        }

    val PauseHabit: ImageVector
        @Composable get() = remember {
            ImageVector.Builder(
                name = "PauseIcon",
                defaultWidth = 20.dp, defaultHeight = 20.dp,
                viewportWidth = 20f, viewportHeight = 20f
            ).addPath(
                pathData = PathParser().parsePathString(
                    "M7 14H9V6H7V14V14M11 14H13V6H11V14V14M10 20C8.61667 20 7.31667 19.7375 6.1 19.2125C4.88333 18.6875 3.825 17.975 2.925 17.075C2.025 16.175 1.3125 15.1167 0.7875 13.9C0.2625 12.6833 0 11.3833 0 10C0 8.61667 0.2625 7.31667 0.7875 6.1C1.3125 4.88333 2.025 3.825 2.925 2.925C3.825 2.025 4.88333 1.3125 6.1 0.7875C7.31667 0.2625 8.61667 0 10 0C11.3833 0 12.6833 0.2625 13.9 0.7875C15.1167 1.3125 16.175 2.025 17.075 2.925C17.975 3.825 18.6875 4.88333 19.2125 6.1C19.7375 7.31667 20 8.61667 20 10C20 11.3833 19.7375 12.6833 19.2125 13.9C18.6875 15.1167 17.975 16.175 17.075 17.075C16.175 17.975 15.1167 18.6875 13.9 19.2125C12.6833 19.7375 11.3833 20 10 20V20M10 18C12.2333 18 14.125 17.225 15.675 15.675C17.225 14.125 18 12.2333 18 10C18 7.76667 17.225 5.875 15.675 4.325C14.125 2.775 12.2333 2 10 2C7.76667 2 5.875 2.775 4.325 4.325C2.775 5.875 2 7.76667 2 10C2 12.2333 2.775 14.125 4.325 15.675C5.875 17.225 7.76667 18 10 18V18M10 10V10V10V10V10V10V10V10V10V10"
                ).toNodes(),
                fill = SolidColor(Color(0xFFC2C6D6))
            ).build()
        }

    val DeleteProtocol: ImageVector
        @Composable get() = remember {
            ImageVector.Builder(
                name = "DeleteIcon",
                defaultWidth = 10.dp, defaultHeight = 11.dp,
                viewportWidth = 10f, viewportHeight = 11f
            ).addPath(
                pathData = PathParser().parsePathString(
                    "M1.75 10.5C1.42917 10.5 1.15451 10.3858 0.926042 10.1573C0.697569 9.92882 0.583333 9.65417 0.583333 9.33333V1.75H0V0.583333H2.91667V0H6.41667V0.583333H9.33333V1.75H8.75V9.33333C8.75 9.65417 8.63576 9.92882 8.40729 10.1573C8.17882 10.3858 7.90417 10.5 7.58333 10.5H1.75V10.5M7.58333 1.75H1.75V9.33333V9.33333V9.33333H7.58333V9.33333V9.33333V1.75V1.75M2.91667 8.16667H4.08333V2.91667H2.91667V8.16667V8.16667M5.25 8.16667H6.41667V2.91667H5.25V8.16667V8.16667M1.75 1.75V1.75V9.33333V9.33333V9.33333V9.33333V9.33333V9.33333V1.75V1.75"
                ).toNodes(),
                fill = SolidColor(Color(0xFFFFB4AB))
            ).build()
        }

    val MatrixLogo: ImageVector
        @Composable get() = AppLogo
}


