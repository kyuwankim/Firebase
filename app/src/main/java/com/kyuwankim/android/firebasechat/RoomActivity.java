package com.kyuwankim.android.firebasechat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {

    TextView roomTitle;
    RecyclerView listView;
    CustomAdapter adapter;

    EditText editMessage;
    Button btnSend;

    // 데이터베이스 연결
    FirebaseDatabase database;
    DatabaseReference roomRef;

    List<Message> datas = new ArrayList<>();

    String userid;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // 목록에서 넘어온 인텐트
        Intent intent = getIntent();
        String key = intent.getExtras().getString("key");
        String title = intent.getExtras().getString("title");
        userid = intent.getExtras().getString("userid");
        username = intent.getExtras().getString("username");

        // 위젯
        roomTitle = (TextView) findViewById(R.id.roomTitle);
        listView = (RecyclerView) findViewById(R.id.listView);
        adapter = new CustomAdapter(this, datas, userid);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        editMessage = (EditText) findViewById(R.id.editMessage);
        btnSend = (Button) findViewById(R.id.btnSend);

        // 방이름
        roomTitle.setText(title);

        // 데이터베이스 레퍼런스
        database = FirebaseDatabase.getInstance();
        roomRef = database.getReference("chat").child(key);
        roomRef.addValueEventListener(eventListener);

        btnSend.setOnClickListener(sendListener);
    }

    // 채팅 전송처리
    View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String msgKey = roomRef.push().getKey();
            String msg = editMessage.getText().toString();

            DatabaseReference msgRef = roomRef.child(msgKey);

            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("userid",userid);
            msgMap.put("username",username);
            msgMap.put("msg",msg);

            msgRef.setValue(msgMap);
        }
    };

    // 채팅 목록 처리
    ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            datas.clear();
            //   배열중에 한개          :  bbs 아래에 전체 데이터셋이 배열
            for( DataSnapshot snapshot : dataSnapshot.getChildren() ){
                String key = snapshot.getKey();
                Message msg = snapshot.getValue(Message.class);
                msg.setKey(key);

                datas.add(msg);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}

class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.Holder>{
    Context context;
    List<Message> datas;
    String userid;

    public CustomAdapter(Context context, List<Message> datas, String userid){
        this.context = context;
        this.datas = datas;
        this.userid = userid;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Message msg = datas.get(position);
        holder.userName.setText(msg.getUsername());
        holder.msg.setText(msg.getMsg());
        if(!userid.equals(msg.getUserid())){
            holder.itemLayout.setGravity(Gravity.RIGHT);
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        LinearLayout itemLayout;
        TextView userName;
        TextView msg;

        public Holder(View itemView) {
            super(itemView);
            itemLayout = (LinearLayout) itemView.findViewById(R.id.itemLayout);
            userName = (TextView) itemView.findViewById(R.id.userName);
            msg = (TextView) itemView.findViewById(R.id.msg);
        }
    }
}