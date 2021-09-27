package com.anubhav.talkzone2;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private TextInputEditText searchContactsBar;
    private FloatingActionButton addContactButton;
    private RecyclerView listOfContacts;
    private TextView noResultsFound;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference mUsersReference, mContactsReference, mMessagesReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        initializeVariables(view);

        updateUserStatus("online");

        searchForContacts();

        return view;
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessagesReference = FirebaseDatabase.getInstance().getReference().child("Messages");
        mContactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");

        searchContactsBar = view.findViewById(R.id.editTextSearchContacts);
        listOfContacts = view.findViewById(R.id.listOfContacts);
        listOfContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        listOfContacts.addItemDecoration(itemDecoration);
        noResultsFound = view.findViewById(R.id.homeNoResultsFound);

        addContactButton = view.findViewById(R.id.addContactButton);
        addContactButton.setOnClickListener(v -> sendUserToAddContactActivity());

        searchContactsBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForContacts();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mContactsReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
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
    }

    private void searchForContacts() {
        FirebaseRecyclerOptions<Contact> contactFirebaseRecyclerOptions;

        if (searchContactsBar.getText().toString().equals("")) {
            contactFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Contact>()
                    .setQuery(mContactsReference.child(currentUserID), Contact.class)
                    .build();
        }

        else {
            contactFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Contact>()
                    .setQuery(mContactsReference.child(currentUserID).orderByChild("fullName").startAt(searchContactsBar.getText().toString()).endAt(searchContactsBar.getText().toString() + "\uf8ff"), Contact.class)
                    .build();
        }

        Query searchContactsQuery = mContactsReference.child(currentUserID).orderByChild("fullName")
                .startAt(searchContactsBar.getText().toString()).endAt(searchContactsBar.getText().toString() + "\uf8ff");

        searchContactsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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

        FirebaseRecyclerAdapter<Contact, ContactsViewHolder> contactFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(contactFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, final int position, @NonNull Contact contact) {
                final String receiverUserID = getRef(position).getKey();

                mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String name = dataSnapshot.child("fullName").getValue().toString();
                            contactsViewHolder.fullName.setText(name);

                            String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                            if (! profileImage.equals("-1"))
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_black_75).into(contactsViewHolder.profileImage);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                contactsViewHolder.itemView.setOnClickListener(v -> {
                    Intent messagesIntent = new Intent(getActivity(), MessagesActivity.class);
                    messagesIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    messagesIntent.putExtra("visitUserID", receiverUserID);
                    startActivity(messagesIntent);
                });

                mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("userState").child("type").getValue().equals("online"))
                                contactsViewHolder.online.setVisibility(View.VISIBLE);
                            else
                                contactsViewHolder.online.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Query lastQuery = mMessagesReference.child(currentUserID).child(receiverUserID).orderByKey().limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String from = childSnapshot.child("from").getValue().toString();
                            final String message = childSnapshot.child("message").getValue().toString();
                            final String type = childSnapshot.child("type").getValue().toString();
                            String introMessage = "You: ";

                            if (from.equals(currentUserID)) {
                                if (type.equals("text")) {
                                    if ((introMessage + message).length() >= 58) {
                                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage);
                                        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                        spannableStringBuilder.setSpan(boldStyle, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        spannableStringBuilder.append(message.substring(0, 58));
                                        spannableStringBuilder.append("...");
                                        contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                    }

                                    else {
                                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + message);
                                        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                        spannableStringBuilder.setSpan(boldStyle, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                    }
                                }

                                else if (type.equals("image")) {
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "🖼️ Image");
                                    StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                    spannableStringBuilder.setSpan(boldStyle, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                }

                                else {
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "📁 File");
                                    StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                    spannableStringBuilder.setSpan(boldStyle, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                }
                            }

                            else {
                                mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String fullName = dataSnapshot.child("fullName").getValue().toString();
                                        String introMessage = fullName + ": ";

                                        if (type.equals("text")) {
                                            if ((introMessage + message).length() >= 50) {
                                                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage);
                                                StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                                spannableStringBuilder.setSpan(boldStyle, 0, introMessage.indexOf(":"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                                spannableStringBuilder.append(message.substring(0, 50));
                                                spannableStringBuilder.append("...");
                                                contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                            }

                                            else {
                                                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + message);
                                                StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                                spannableStringBuilder.setSpan(boldStyle, 0, introMessage.indexOf(":"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                                contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                            }
                                        }

                                        else if (type.equals("image")) {
                                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "🖼️ Image");
                                            StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                            spannableStringBuilder.setSpan(boldStyle, 0, introMessage.indexOf(":"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                            contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                        }

                                        else {
                                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "📁 File");
                                            StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                            spannableStringBuilder.setSpan(boldStyle, 0, introMessage.indexOf(":"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                            contactsViewHolder.lastMessage.setText(spannableStringBuilder);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_of_contacts, parent, false);
                return new ContactsViewHolder(view);
            }
        };

        listOfContacts.setAdapter(contactFirebaseRecyclerAdapter);
        contactFirebaseRecyclerAdapter.startListening();
    }

    private void sendUserToAddContactActivity() {
        Intent addContactIntent = new Intent(getActivity(), AddContactActivity.class);
        startActivity(addContactIntent);
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private ImageView online;
        private TextView fullName, lastMessage;

        public ContactsViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.allUsersProfileImage);
            online = itemView.findViewById(R.id.allUsersOnline);
            fullName = itemView.findViewById(R.id.allUsersFullName);
            lastMessage = itemView.findViewById(R.id.allUsersLastMessages);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUserStatus("online");
    }

    @Override
    public void onPause() {
        super.onPause();

        updateUserStatus("offline");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        updateUserStatus("offline");
    }

    private void updateUserStatus(String status) {
        String saveCurrentDate, saveCurrentTime;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", status);

        mUsersReference.child(currentUserID).child("userState").updateChildren(currentStateMap);
    }
}