/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.client.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Disposable;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupImpl implements Group, Disposable
{
    private final long id;
    private final WeakReference<JDAImpl> apiRef;

    private final TLongObjectMap<UserImpl> userMap = MiscUtil.newLongMap();

    private CallImpl currentCall;
    private User owner;
    private String name;
    private String iconId;
    private long lastMessageId;
    private boolean disposed = false;

    public GroupImpl(long id, JDAImpl api)
    {
        this.id = id;
        this.apiRef = new WeakReference<>(api);
    }

    @Override
    public long getLatestMessageIdLong()
    {
        final long messageId = lastMessageId;
        if (messageId < 0)
            throw new IllegalStateException("No last message id found.");
        return messageId;
    }

    @Override
    public boolean hasLatestMessage()
    {
        return lastMessageId > 0;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public ChannelType getType()
    {
        return ChannelType.GROUP;
    }

    @Override
    public String getIconId()
    {
        return iconId;
    }

    @Override
    public String getIconUrl()
    {
        return iconId == null ? null : "https://cdn.discordapp.com/channel-icons/" + id + "/" + iconId + ".jpg";
    }

    @Override
    public User getOwner()
    {
        return owner;
    }

    @Override
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        userMap.valueCollection()));
    }

    @Override
    public List<User> getNonFriendUsers()
    {
        List<User> nonFriends = new ArrayList<>();
        TLongObjectMap<Relationship> map = ((JDAClientImpl) getJDA().asClient()).getRelationshipMap();
        userMap.forEachEntry((userId, user) ->
        {
            Relationship relationship = map.get(userId);
            Friend friend = relationship instanceof Friend ? (Friend) relationship : null;
            if (friend == null)
                nonFriends.add(user);
            return true;
        });
        return Collections.unmodifiableList(nonFriends);
    }

    @Override
    public List<Friend> getFriends()
    {
        List<Friend> friends = new ArrayList<>();
        TLongObjectMap<Relationship> map = ((JDAClientImpl) getJDA().asClient()).getRelationshipMap();
        userMap.forEachKey(userId ->
        {
            Relationship relationship = map.get(userId);
            Friend friend = relationship instanceof Friend ? (Friend) relationship : null;
            if (friend != null)
                friends.add(friend);
            return true;
        });
        return Collections.unmodifiableList(friends);
    }

    @Override
    public RestAction<Call> startCall()
    {
        return null;
    }

    @Override
    public Call getCurrentCall()
    {
        return currentCall;
    }

    @Override
    public RestAction leaveGroup()
    {
        return null;
    }

    @Override
    public JDA getJDA()
    {
        return apiRef.get();
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean dispose()
    {
        if (currentCall != null)
            currentCall.dispose();
        return disposed = true;
    }

    @Override
    public boolean isDisposed()
    {
        return disposed;
    }

    @Override
    public String toString()
    {
        return String.format("G:%s(%d)", getName(), id);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof GroupImpl))
            return false;

        GroupImpl oGroup = (GroupImpl) o;
        return id == oGroup.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    public TLongObjectMap<UserImpl> getUserMap()
    {
        return userMap;
    }

    public GroupImpl setCurrentCall(CallImpl call)
    {
        this.currentCall = call;
        return this;
    }

    public GroupImpl setOwner(User owner)
    {
        this.owner = owner;
        return this;
    }

    public GroupImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GroupImpl setIconId(String iconId)
    {
        this.iconId = iconId;
        return this;
    }

    public GroupImpl setLastMessageId(long lastMessageId)
    {
        this.lastMessageId = lastMessageId;
        return this;
    }

    private void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }
}
