package it.feio.android.omninotes.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import it.feio.android.omninotes.BaseFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.databinding.PdfTemplateTextNoteBinding;

public class NoteToPdfHelper {

    public Intent convertStringToPDF(String head, String contents, Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.pdf_template_text_note, null);

        TextView headView = view.findViewById(R.id.txtHead);
        TextView contextView = view.findViewById(R.id.txtContext);

        headView.setText(head);
        contextView.setText(contents);

        PdfDocument pdf = createPdfFromView(view, context);

        File file = createFilePath(head);

        if(file != null) {
            writePdfToFillePath(pdf, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri,"application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            return intent;
        }
        else{
            return null;
        }
    }

    public File createFilePath(String head){
        if(head.contains(".") || head.contains("/")){
            return null;
        }
        else {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), head + ".pdf");
        }
    }

    public void writePdfToFillePath(PdfDocument pdf,File file){
        try {
            FileOutputStream fileOutPut = new FileOutputStream(file);
            pdf.writeTo(fileOutPut);
            pdf.close();
            fileOutPut.flush();
            fileOutPut.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            pdf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            pdf.close();
        }
    }

    public PdfDocument createPdfFromView(View view,Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getDisplay().getRealMetrics(displayMetrics);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(
                        displayMetrics.widthPixels, View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                        displayMetrics.heightPixels, View.MeasureSpec.EXACTLY
                )
        );

        view.layout(0,0,displayMetrics.widthPixels,displayMetrics.heightPixels);

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        Bitmap.createScaledBitmap(bitmap,view.getMeasuredWidth(),view.getMeasuredHeight(),true);

        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getMeasuredWidth(),view.getMeasuredHeight(),1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);

        Canvas canvasPage = page.getCanvas();
        canvasPage.drawBitmap(bitmap,0,0,null);
        pdf.finishPage(page);

        return pdf;
    }
}
