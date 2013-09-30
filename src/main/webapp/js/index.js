
var COLOR_STRENGTH = "#906030";
var COLOR_LAND = "#009000";
var COLOR_ENERGY = "#0000FF";
var COLOR_RAGE = "#FF0000";

function getstats()
{
    $.ajax(
    {
        url: "getstats",
        method: "post",
        dataType: "json",
        success: function (data)
        {
            $("#username").text(data.username);
            
            $("#stats-value-score").text(data.score);
            // Cap is 100%...
            data.strength = data.strength > 1 ? 1 : data.strength;
            data.land = data.land > 1 ? 1 : data.land;
            data.energy = data.energy > 1 ? 1 : data.energy;
            drawStat("strength", parseFloat(data.strength));
            drawStat("land", parseFloat(data.land));
            drawStat("energy", parseFloat(data.energy));
            drawStat("rage", parseFloat(data.rage));
            
            if (data.energy < 0.1)
                $("#boost-strength, #boost-land").attr("disabled", "disabled");
            else
                $("#boost-strength, #boost-land").removeAttr("disabled");
                
            switch (data.task)
            {
                case 'S':
                    $("#task-strength").attr("disabled", "disabled");
                    $("#task-land").removeAttr("disabled");
                    break;
                case 'L':
                    $("#task-strength").removeAttr("disabled");
                    $("#task-land").attr("disabled", "disabled");
                    break;
            }
            
            var d = new Date(data.lastupdate);
            d.setMinutes(d.getMinutes()+1);
            $("#stats-value-nextupdate").text((d.getHours() < 10 ? "0" : "") + d.getHours() + ":"
                + (d.getMinutes() < 10 ? "0" : "") + d.getMinutes() + ":"
                + (d.getSeconds() < 10 ? "0" : "") + d.getSeconds());
        }
    });
    $.ajax(
    {
        url: "gettargets",
        method: "post",
        dataType: "json",
        success: function (data)
        {
            $("#targets tbody").empty();
            for (var i = 0; i < data.length; i++)
            {
                var tr = $("<tr>");
                var td = $("<td>");
                td.attr("username", data[i].username);
                td.html(data[i].username);
                td.addClass("username");
                td.click(function ()
                {
                    var userToAttack = $(this).attr("username");
                    if (userToAttack !== null && userToAttack.length > 0)
                        attack(userToAttack);
                });
                tr.append(td);
                var td = $("<td>");
                var race = data[i].race;
                td.html(race);
                td.addClass("race");
                td.addClass("race-"+race);
                tr.append(td);
                var td = $("<td>");
                var land = data[i].land;
                land = land > 1 ? 1 : land;
                td.html(parseFloat(land * 100).toFixed(1) + "%");
                td.addClass("land");
                tr.append(td);
                var td = $("<td>");
                if (data[i].lastAttacker !== null && data[i].lastAttacker.length > 0)
                    td.html(data[i].lastAttacker);
                td.addClass("last-attacker");
                tr.append(td);
                $("#targets tbody").append(tr);
            }
        }
    });
    $.ajax(
    {
        url: "getscores",
        method: "post",
        dataType: "json",
        success: function (data)
        {
            $("#scorebox-values tbody").empty();
            for (var i = 0; i < data.length; i++)
            {
                var tr = $("<tr>");
                var td = $("<td>");
                td.html(data[i].score);
                tr.append(td);
                var td = $("<td>");
                td.html(data[i].username);
                tr.append(td);
                $("#scorebox-values tbody").append(tr);
            }
        }
    });
}

function attack(user)
{
//    if (!confirm("Confirm attack to " + user + "?"))
//        return;

    $.ajax(
    {
        url: "attack",
        method: "post",
        data: {"attackeduser": user},
        dataType: "json",
        success: function (data)
        {
            if (data.attackerStrengthTooLow && data.attackerEnergyTooLow)
            {
                alert("Both your strength and energy are too low. (minimum required: 10%)");
                return;
            }
            else if (data.attackerStrengthTooLow)
            {
                alert("Your strength is too low. (minimum required: 10%)");
                return;
            }
            else if (data.attackerEnergyTooLow)
            {
                alert("Your energy is too low. (minimum required: 10%)");
                return;
            }
            
            if (data.defenderLandTooLow)
            {
                alert("Not enough land on target. (must be at least 10%)");
                return;
            }
            
            if (data.tie)
            {
                $("#last-attack-result").html(
                      "Attack was a tie, result ~= "+parseFloat(data.result * 100).toFixed(1)+"%. "
                     +"At least 1% of difference needed.");
                return;
            }
            
            var won = data.result > 0;
            
            $("#last-attack-result").html("Attack result: <br/><br/>"
                 +(parseFloat(data.result) * 100).toFixed(1) + "%"
                 +(won ? " <span style=\"color:#009000; font-weight: bold\">[WON]</span>" : " <span style=\"color:#FF0000; font-weight: bold\">[LOST]</span>")
                 +"<br/><br/>Delta Strength: "+(parseFloat(data.strength) * 100).toFixed(1) + "%"
                 +"<br/><br/>Delta Land: "+(parseFloat(data.land) * 100).toFixed(1) + "%"
                 +"<br/><br/>Delta Energy: "+(parseFloat(data.energy) * 100).toFixed(1) + "%");
            getstats();
        }
    });
}

function logout()
{
    $.ajax(
    {
        url: "logout",
        method: "post",
        success: function ()
        {
            location.href = "loginform.html";
        }
    });
}

function spendenergy(task)
{
    switch (task)
    {
        case "S":
            if (!confirm("Do you want to spend 10% of your energy for 5% of strength?"))
                return;
            break;
        case "L":
            if (!confirm("Do you want to spend 10% of your energy for 5% of land?"))
                return;
            break;
        default:
            return;
    }
    
    $.ajax(
    {
        url: "spendenergy",
        method: "post",
        data: {"task": task},
        success: getstats
    });
}

function boostrage()
{
    if (!confirm("Do you want to spend 10% of your SCORE POINTS (minimum total required: 100) for 10% of rage?"))
        return;
    
    $.ajax(
    {
        url: "boostrage",
        method: "post",
        success: getstats
    });
}

function drawStat(type, value)
{
    var id = "";
    var color = "";
    switch (type)
    {
        case "strength":
            id = "stats-value-strength";
            color = COLOR_STRENGTH;
            break;
        case "land":
            id = "stats-value-land";
            color = COLOR_LAND;
            break;
        case "energy":
            id = "stats-value-energy";
            color = COLOR_ENERGY;
            break;
        case "rage":
            id = "stats-value-rage";
            color = COLOR_RAGE;
            break;
        default:
            return;
    }
    
    var element = $("canvas#"+id);
    var w = element.width();
    var h = element.height();
    var minlen = w < h ? w : h;
    element.clearCanvas();
    element.drawArc(
    {
        strokeStyle: color,
        strokeWidth: minlen/16,
        x: w/2, y: h/2,
        radius: minlen/2 - minlen/16,
        // start and end angles in degrees
        start: 0, end: value*360
    })
    .drawText(
    {
        fillStyle: color,
        x: w/2, y: h/2,
        fontSize: minlen/4.5,
        fontFamily: "sans-serif",
        text: (value *100).toFixed(1)
    });
}

$(document).ready(function ()
{
    $("#stats-legend-strength").css({"background-color": COLOR_STRENGTH});
    $("#stats-legend-land").css({"background-color": COLOR_LAND});
    $("#stats-legend-energy").css({"background-color": COLOR_ENERGY});
    $("#stats-legend-rage").css({"background-color": COLOR_RAGE});
    
    $.ajax(
    {
        url: "isloggedin",
        method: "post",
        dataType: "json",
        success: function (data)
        {
            if (!data)
                location.href = "loginform.html";
        }
    });

    $("#task-strength").click(function ()
    {
        $.ajax(
        {
            url: "changetask",
            method: "post",
            data: {"task": "S"},
            success: getstats
        });
    });

    $("#task-land").click(function ()
    {
        $.ajax(
        {
            url: "changetask",
            method: "post",
            data: {"task": "L"},
            success: getstats
        });
    });
    
    $("#boost-rage").click(function (){boostrage();});
    $("#boost-strength").click(function (){spendenergy("S");});
    $("#boost-land").click(function (){spendenergy("L");});

    getstats();
    var timer = setInterval(getstats, 30000);
});
