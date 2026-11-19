package id.rainticket.service;
import id.rainticket.model.Booking;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;
public class TicketPdfService {
 public File create(Booking booking) throws Exception {
  File folder=new File("tickets"); if(!folder.exists()) folder.mkdirs();
  File file=new File(folder,booking.getCode()+".pdf");
  try(PDDocument doc=new PDDocument()){ PDPage page=new PDPage(new PDRectangle(420,595)); doc.addPage(page);
   try(PDPageContentStream cs=new PDPageContentStream(doc,page)){
    write(cs,PDType1Font.HELVETICA_BOLD,22,35,550,"RAIL TICKET");
    write(cs,PDType1Font.HELVETICA,10,35,530,"E-TICKET / BOARDING PASS");
    cs.moveTo(35,515);cs.lineTo(385,515);cs.stroke();
    String[] lines=booking.new BoardingPass().print().split("\\n"); float y=485;
    for(String line:lines){write(cs,PDType1Font.HELVETICA,11,35,y,line.replace("→","-").replace("–","-"));y-=25;}
    cs.moveTo(35,90);cs.lineTo(385,90);cs.stroke();write(cs,PDType1Font.HELVETICA_OBLIQUE,9,35,70,"Tunjukkan tiket ini saat boarding.");
   } doc.save(file); } return file;
 }
 private void write(PDPageContentStream cs,PDType1Font font,int size,float x,float y,String text)throws Exception{cs.beginText();cs.setFont(font,size);cs.newLineAtOffset(x,y);cs.showText(text.replace("Rp","IDR"));cs.endText();}
}
