
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
            $("#stats-value-strength").text((data.strength * 100).toFixed(1) + "%");
            $("#stats-value-land").text((data.land * 100).toFixed(1) + "%");
            $("#stats-value-energy").text((data.energy * 100).toFixed(1) + "%");
            $("#stats-value-rage").text((data.rage * 100).toFixed(1) + "%");

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

$(document).ready(function ()
{
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
    
    $("#boost-strength").click(function (){spendenergy("S");});
    $("#boost-land").click(function (){spendenergy("L");});

    getstats();
    var timer = setInterval(getstats, 10000);
});
