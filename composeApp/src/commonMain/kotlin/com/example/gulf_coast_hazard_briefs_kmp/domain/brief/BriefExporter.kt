package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

object BriefExporter {

    fun exportMarkdown(pages: List<BriefPage>): String {
        val sb = StringBuilder()

        sb.append("# Weekly Gulf Coast Hazard Brief\n\n")

        pages.sortedBy { it.pageNumber }.forEach { page ->
            sb.append("## Page ${page.pageNumber} — ${page.title}\n\n")

            // Prefer structured render if available
            val body = page.renderBody().trim()
            if (body.isNotBlank()) {
                body.lines().forEach { line ->
                    sb.append("- ").append(line).append("\n")
                }
            } else {
                sb.append("_No content available._\n")
            }

            sb.append("\n")
        }

        return sb.toString().trim()
    }

    fun exportPlainText(pages: List<BriefPage>): String {
        val sb = StringBuilder()

        sb.append("WEEKLY GULF COAST HAZARD BRIEF\n\n")

        pages.sortedBy { it.pageNumber }.forEach { page ->
            sb.append("PAGE ${page.pageNumber}: ${page.title}\n")
            sb.append("--------------------------------\n")
            sb.append(page.renderBody().trim())
            sb.append("\n\n")
        }

        return sb.toString().trim()
    }
}