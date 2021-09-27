package com.anubhav.talkzone2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallHistoryFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String currentUserID, receiverUserID;
    private DatabaseReference mCallHistoryReference;

    private RecyclerView listOfCalls;
    private TextView noResultsFound;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_call_history, container, false);

        initializeVariables(view);

        return view;
    }

    private void initializeVariables(View v) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mCallHistoryReference = FirebaseDatabase.getInstance().getReference().child("Call History");

        noResultsFound = v.findViewById(R.id.callHistoryNoResultsFound);

        listOfCalls = v.findViewById(R.id.listOfCalls);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        listOfCalls.setLayoutManager(linearLayoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        listOfCalls.addItemDecoration(itemDecoration);

        FirebaseRecyclerOptions<CallHistory> callHistoryFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<CallHistory>()
                .setQuery(mCallHistoryReference.child(currentUserID), CallHistory.class)
                .build();

        Query callHistoryQuery = mCallHistoryReference.child(currentUserID);

        callHistoryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    noResultsFound.setVisibility(View.GONE);

                else
                    noResultsFound.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerAdapter<CallHistory, CallHistoryViewHolder> callHistoryFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<CallHistory, CallHistoryViewHolder>(callHistoryFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CallHistoryViewHolder callHistoryViewHolder, int position, @NonNull final CallHistory callHistory) {
                callHistoryViewHolder.fullName.setText(callHistory.getFullName());

                if (! callHistory.getProfileImage().equals("-1"))
                    Picasso.get().load(callHistory.getProfileImage()).placeholder(R.drawable.ic_baseline_person_black_50).into(callHistoryViewHolder.profileImage);

                callHistoryViewHolder.type.setImageResource(R.drawable.ic_baseline_phone_black_16);

                String dateCalled = callHistory.getDate();
                String timeCalled = callHistory.getTime();

                DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -1);
                String yesterdayDate = dateFormat.format(calendar.getTime());

                String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

                if (dateCalled.equals(currentDate))
                    callHistoryViewHolder.dateAndTime.setText(timeCalled);

                else if (dateCalled.equals(yesterdayDate))
                    callHistoryViewHolder.dateAndTime.setText("Yesterday");

                else {
                    try {
                        if (isDateInCurrentWeek(dateFormat.parse(dateCalled))) {
                            Date date = dateFormat.parse(dateCalled);
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEEE");
                            callHistoryViewHolder.dateAndTime.setText(dateFormat2.format(date));
                        }

                        else
                            callHistoryViewHolder.dateAndTime.setText(dateCalled);
                    }

                    catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                callHistoryViewHolder.informationButton.setOnClickListener(v1 -> {
                    receiverUserID = callHistory.getReceiverUserID();
                    sendUserToProfileActivity();
                });
            }

            @NonNull
            @Override
            public CallHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_of_call_history, parent, false);
                return new CallHistoryViewHolder(view);
            }
        };

        listOfCalls.setAdapter(callHistoryFirebaseRecyclerAdapter);
        callHistoryFirebaseRecyclerAdapter.startListening();
    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
        profileIntent.putExtra("receiverUserID", receiverUserID);
        startActivity(profileIntent);
    }

    private boolean isDateInCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return week == targetWeek && year == targetYear;
    }

    public static class CallHistoryViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private ImageButton informationButton;
        private ImageView type;
        private TextView fullName, dateAndTime;

        public CallHistoryViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.callProfileImage);
            informationButton = itemView.findViewById(R.id.informationButton);
            type = itemView.findViewById(R.id.callType);
            fullName = itemView.findViewById(R.id.callFullName);
            dateAndTime = itemView.findViewById(R.id.callDateAndTime);
        }
    }
}