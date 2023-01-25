/*
 * Copyright 1999-2023 Percussion Software, Inc.
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.utils.container;

import com.percussion.utils.string.PSStringUtils;
import org.apache.commons.lang.StringUtils;

public class PSJndiDatasourceImpl implements IPSJndiDatasource {

    protected String name;
    protected String driverName;
    protected String server;
    protected String userId;
    protected String password;
    protected String driverClassName;
    protected String securityDomain;
    protected int minConnections=15;
    protected int maxConnections=100;
    protected int idleTimeout=59000;
    protected String connectionTestQuery;
    protected boolean isEncrypted;
    private int id;

    public PSJndiDatasourceImpl()
    {

    }

    public PSJndiDatasourceImpl(String name, String driverName, String driverClassName, String server, String uid, String pwd) {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name may not be null or empty");

        if (StringUtils.isBlank(driverName))
            throw new IllegalArgumentException(
                    "driverName may not be null or empty");

        if (StringUtils.isBlank(driverClassName))
            throw new IllegalArgumentException(
                    "driverClassName may not be null or empty");



        if (StringUtils.isBlank(server))
            throw new IllegalArgumentException("server may not be null or empty");


        this.name=name;
        this.driverName=driverName;
        this.driverClassName=driverClassName;
        this.server=server;
        this.userId=uid;
        this.password=pwd;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name=name;
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getDriverClassName() {
        return driverClassName;
    }

    @Override
    public void setDriverClassName(String driverClassName) {
        this.driverClassName=driverClassName;
    }

    @Override
    public String getSecurityDomain() {
        return securityDomain;
    }

    @Override
    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
    }

    @Override
    public void setMinConnections(int min) {
        this.minConnections = min;
    }

    @Override
    public int getMinConnections() {
        return this.minConnections;
    }

    @Override
    public void setMaxConnections(int max) {
        this.maxConnections = max;
    }

    @Override
    public int getMaxConnections() {
        return maxConnections;
    }

    @Override
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @Override
    public int getIdleTimeout() {
        return idleTimeout;
    }

    @Override
    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    @Override
    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }

    @Override
    public boolean isEncrypted() {
        return isEncrypted;
    }


    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }

    @Override
    public String toString() {
        return "PSJndiDatasourceImpl{" +
                "name='" + name + '\'' +
                ", driverName='" + driverName + '\'' +
                ", server='" + server + '\'' +
                ", userId='" + userId + '\'' +
                ", password='" + PSStringUtils.hidePass(password) + '\'' +
                ", driverClassName='" + driverClassName + '\'' +
                ", securityDomain='" + securityDomain + '\'' +
                ", minConnections=" + minConnections +
                ", maxConnections=" + maxConnections +
                ", idleTimeout=" + idleTimeout +
                ", connectionTestQuery='" + connectionTestQuery + '\'' +
                ", isEncrypted=" + isEncrypted +
                ", id=" + id +
                '}';
    }
}
