package se.acrend.ppm.handler

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.PpmFund
import se.acrend.ppm.repository.PpmFundRepository
import java.io.File


/**
 *
 */
@Component
class AdminHandler(val ppmFundRepository: PpmFundRepository) {

    fun handleFundList(request: ServerRequest): Mono<ServerResponse> {

        val sheetName = request.body(BodyExtractors.toMultipartData())
                .flatMap { map ->
                    Mono.just(map.get("fundListFile")?.get(0))
                }
                .flatMap { part ->
                    if (part is FilePart) {
                        val filePart = part as FilePart

                        val file = File.createTempFile("fundlist", ".xls")

                        file.deleteOnExit()

                        filePart.transferTo(file)

                        val fs = NPOIFSFileSystem(file)
                        val wb = HSSFWorkbook(fs.getRoot(), true)

                        Mono.just(wb)
                    } else {
                        Mono.empty()
                    }
                }
                .flatMap { wb ->

                    val sheet = wb.getSheetAt(0)

                    val headerRow = sheet.getRow(0)

                    val fundEntities = ArrayList<PpmFund>()

                    if ("fondnamn".equals(headerRow.getCell(2).stringCellValue.toLowerCase()) &&
                            "fondstatus".equals(headerRow.getCell(16).stringCellValue.toLowerCase())) {

                        for (row in sheet) {
                            if (row.isActiveFund()) {
                                fundEntities.add(transform(row))
                            }
                        }
                    }

                    ppmFundRepository.deleteAll().subscribe()
                    ppmFundRepository.saveAll(Flux.fromIterable(fundEntities)).subscribe()

                    Mono.just(sheet.sheetName)
                }


        return ServerResponse.ok().body(sheetName, String::class.java)
    }

    private fun transform(row: Row): PpmFund {
        with(row) {
            return PpmFund(cellValue(2), cellValue(1),
                    cellValue(3), cellValue(8))
        }
    }

    fun Row.isActiveFund(): Boolean {
        val cell = this.getCell(16)
        val cellType = cell.cellTypeEnum
        if (cellType == CellType.NUMERIC) {
            return cell.numericCellValue.toInt() == 1
        }
        return false
    }

    fun Row.cellValue(index: Int): String {
        val cell = this.getCell(index)

        return when (cell.cellTypeEnum) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toInt().toString()
            else -> "<${cell.cellTypeEnum}>"

        }
    }

}