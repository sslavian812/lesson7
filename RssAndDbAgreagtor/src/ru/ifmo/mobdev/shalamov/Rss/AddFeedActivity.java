package ru.ifmo.mobdev.shalamov.Rss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 09.11.13
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class AddFeedActivity extends Activity {

    public static final int RESULT_ADD = 0;
    public static final int RESULT_UPD = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_feed);

        final Button btn = (Button) findViewById(R.id.btnok);
        final EditText edtName = (EditText) findViewById(R.id.edtName);
        final EditText edtLink = (EditText) findViewById(R.id.edtLink);


        Intent intent = getIntent();
        final int code;

        if (intent.getIntExtra(FeedActivity.KEY_ID, -1) != -1)
            code = RESULT_UPD;
        else
            code = RESULT_ADD;


        if (code == RESULT_UPD) {
            edtName.setText(intent.getStringExtra(FeedActivity.KEY_NAME));
            edtLink.setText(intent.getStringExtra(FeedActivity.KEY_LINK));

        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtName.getText().toString();
                String link = edtLink.getText().toString();

                name.replaceAll("/*", "");
                link.replaceAll("/*", "");

                if (name.length() == 0 || link.length() == 0) {
                    name = "bash";
                    link = "http://bash.im/rss";
                }

                if (link.length() < 9 || !"http://".equals(link.substring(0, 7)))
                    link = "http://" + link;


                Intent intent = getIntent();
                intent.putExtra(FeedActivity.KEY_NAME, name);
                intent.putExtra(FeedActivity.KEY_LINK, link);
                setResult(code, intent);

                finish();
            }
        });
    }
}
